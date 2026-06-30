package com.github.ringoame196_s_mcPlugin

import org.bukkit.plugin.Plugin
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.concurrent.Executors
import kotlin.io.use

class DataBaseManager(private val plugin: Plugin, dbName: String) {
    private val dbFile: File = File(plugin.dataFolder, dbName)
    private val executor = Executors.newSingleThreadExecutor() // SQLiteはシングルスレッドが安全

    private lateinit var connection: Connection

    fun init() {
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }

        connection = DriverManager.getConnection("jdbc:sqlite:${dbFile.path}")

        val schema = loadSQL("schema.sql")
        connection.createStatement().use { stmt ->
            stmt.executeUpdate(schema)
        }

        // SQLite最適化設定
        connection.createStatement().use { stmt ->
            stmt.execute("PRAGMA journal_mode=WAL;")
            stmt.execute("PRAGMA synchronous=NORMAL;")
            stmt.execute("PRAGMA foreign_keys=ON;")
        }
    }

    fun close() {
        try {
            connection.close()
            executor.shutdown()
        } catch (e: SQLException) {
            plugin.logger.severe("DB Close Error: ${e.message}")
        }
    }

    private fun loadSQL(fileName: String): String {
        return plugin.getResource(fileName)?.bufferedReader()?.use { it.readText() }
            ?: error("SQL file not found: $fileName")
    }

    /**
     * 非同期 UPDATE (INSERT / UPDATE / DELETE)
     */
    fun executeUpdate(sql: String, params: List<Any> = emptyList()) {
        executor.execute {
            try {
                connection.prepareStatement(sql).use { stmt ->
                    params.bind(stmt)
                    stmt.executeUpdate()
                }
            } catch (e: SQLException) {
                plugin.logger.severe("SQL Update Error: ${e.message}")
            }
        }
    }

    /**
     * 非同期 SELECT（複数行）
     */
    fun query(
        sql: String,
        params: List<Any> = emptyList(),
        handler: (List<Map<String, Any?>>) -> Unit
    ) {
        executor.execute {
            val results = mutableListOf<Map<String, Any?>>()

            try {
                connection.prepareStatement(sql).use { stmt ->
                    params.bind(stmt)
                    stmt.executeQuery().use { rs ->
                        val meta = rs.metaData
                        val columnCount = meta.columnCount

                        while (rs.next()) {
                            val row = mutableMapOf<String, Any?>()
                            for (i in 1..columnCount) {
                                val key = meta.getColumnName(i)
                                row[key] = rs.getObject(i)
                            }
                            results.add(row)
                        }
                    }
                }
            } catch (e: SQLException) {
                plugin.logger.severe("SQL Query Error: ${e.message}")
            }

            // メインスレッドに戻す
            plugin.server.scheduler.runTask(
                plugin,
                Runnable {
                    handler(results)
                }
            )
        }
    }

    /**
     * 単一値取得
     */
    fun querySingle(
        sql: String,
        params: List<Any> = emptyList(),
        handler: (Any?) -> Unit
    ) {
        query(sql, params) { list ->
            val value = list.firstOrNull()?.values?.firstOrNull()
            handler(value)
        }
    }

    /**
     * PreparedStatement バインド
     */
    private fun List<Any>.bind(stmt: PreparedStatement) {
        this.forEachIndexed { index, value ->
            stmt.setObject(index + 1, value)
        }
    }
}
