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

    private val dbFile = File(plugin.dataFolder, dbName)
    private val executor = Executors.newSingleThreadExecutor()

    fun init() {
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }

        useConnection { connection ->
            val schema = loadSQL("schema.sql")

            connection.createStatement().use { stmt ->
                stmt.executeUpdate(schema)
            }
        }
    }

    fun close() {
        executor.shutdown()
    }

    /**
     * Connectionを取得して自動で閉じる
     */
    private fun <T> useConnection(block: (Connection) -> T): T {
        DriverManager.getConnection("jdbc:sqlite:${dbFile.path}").use { connection ->

            // Connection毎に設定
            connection.createStatement().use { stmt ->
                stmt.execute("PRAGMA foreign_keys = ON;")
                stmt.execute("PRAGMA journal_mode = WAL;")
                stmt.execute("PRAGMA synchronous = NORMAL;")
            }

            return block(connection)
        }
    }

    private fun loadSQL(fileName: String): String {
        return plugin.getResource(fileName)
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: error("SQL file not found: $fileName")
    }

    /**
     * INSERT / UPDATE / DELETE
     */
    fun executeUpdate(
        sql: String,
        params: List<Any> = emptyList()
    ) {
        executor.execute {
            try {
                useConnection { connection ->
                    connection.prepareStatement(sql).use { stmt ->
                        params.bind(stmt)
                        stmt.executeUpdate()
                    }
                }
            } catch (e: SQLException) {
                plugin.logger.severe("SQL Update Error: ${e.message}")
            }
        }
    }

    /**
     * SELECT
     */
    fun query(
        sql: String,
        params: List<Any> = emptyList(),
        handler: (List<Map<String, Any?>>) -> Unit
    ) {
        executor.execute {

            val results = mutableListOf<Map<String, Any?>>()

            try {
                useConnection { connection ->
                    connection.prepareStatement(sql).use { stmt ->

                        params.bind(stmt)

                        stmt.executeQuery().use { rs ->
                            val meta = rs.metaData

                            while (rs.next()) {
                                val row = mutableMapOf<String, Any?>()

                                for (i in 1..meta.columnCount) {
                                    row[meta.getColumnName(i)] = rs.getObject(i)
                                }

                                results.add(row)
                            }
                        }
                    }
                }
            } catch (e: SQLException) {
                plugin.logger.severe("SQL Query Error: ${e.message}")
            }

            plugin.server.scheduler.runTask(plugin, Runnable { handler(results) })
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
        query(sql, params) { rows ->
            handler(rows.firstOrNull()?.values?.firstOrNull())
        }
    }

    /**
     * PreparedStatementへ値をセット
     */
    private fun List<Any>.bind(stmt: PreparedStatement) {
        forEachIndexed { index, value ->
            stmt.setObject(index + 1, value)
        }
    }
}
