package dev.fritz2.history

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.action
import dev.fritz2.binding.handledBy
import dev.fritz2.dom.html.render
import dev.fritz2.dom.mount
import dev.fritz2.identification.uniqueId
import dev.fritz2.test.initDocument
import dev.fritz2.test.runTest
import dev.fritz2.test.targetId
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlin.browser.document
import kotlin.test.Test
import kotlin.test.assertEquals

class HistoryTests {

    @Test
    fun testSyncedHistory() = runTest {
        initDocument()

        val valueId = "value-${uniqueId()}"
        val historyId = "history-${uniqueId()}"
        val availableId = "available-${uniqueId()}"
        val values = listOf("A", "B", "C", "D")

        fun getValue() = document.getElementById(valueId)?.textContent
        fun getHistory() = document.getElementById(historyId)?.textContent
        fun getAvailable() = document.getElementById(availableId)?.textContent?.toBoolean()

        val store = object : RootStore<String>(values[0]) {
            val hist = history<String>().sync(this)

        }

        render {
            div {
                span(id = valueId) { store.data.bind() }
                span(id = historyId) { store.hist.map { hist -> hist.joinToString() }.bind() }
                span(id = availableId) { store.hist.available.map { it.toString() }.bind() }
            }
        }.mount(targetId)

        delay(100)
        assertEquals(values[0], getValue())
        assertEquals("", getHistory())
        assertEquals(false, getAvailable())

        action(values[1]) handledBy store.update
        delay(100)
        assertEquals(values[1], getValue())
        assertEquals(values[0], getHistory())
        assertEquals(true, getAvailable())

        action(values[2]) handledBy store.update
        delay(100)
        assertEquals(values[2], getValue())
        assertEquals(values.slice(0..1).reversed().joinToString(), getHistory())

        action(values[3]) handledBy store.update
        delay(100)
        assertEquals(values[3], getValue())
        assertEquals(values.slice(0..2).reversed().joinToString(), getHistory())

        action(store.hist.back()) handledBy store.update
        delay(100)
        assertEquals(values[2], getValue())
        assertEquals(values.slice(0..1).reversed().joinToString(), getHistory())

        assertEquals(store.hist.last(), values[1])

        action(store.hist.back()) handledBy store.update
        delay(100)
        assertEquals(values[1], getValue())
        assertEquals(values[0], getHistory())

        store.hist.reset()
        delay(100)
        assertEquals("", getHistory())
        assertEquals(false, getAvailable())
    }

    @Test
    fun testHistoryLongerMax() = runTest {
        initDocument()

        val valueId = "value-${uniqueId()}"
        val historyId = "history-${uniqueId()}"
        val values = listOf("A", "B", "C", "D", "E", "F", "G")

        fun getValue() = document.getElementById(valueId)?.textContent
        fun getHistory() = document.getElementById(historyId)?.textContent

        val histLength = 4

        val store = object : RootStore<String>("") {
            val hist = history<String>(histLength).sync(this)

        }

        render {
            div {
                span(id = valueId) { store.data.bind() }
                span(id = historyId) { store.hist.map { hist -> hist.joinToString() }.bind() }
            }
        }.mount(targetId)

        delay(100)
        assertEquals("", getValue())
        assertEquals("", getHistory())

        values.forEach { value ->
            action(value) handledBy store.update
            delay(1)
        }
        delay(200)

        assertEquals(values.last(), getValue())
        assertEquals(values.takeLast(histLength + 1).reversed().drop(1).joinToString(), getHistory())
    }


    @Test
    fun testManualHistory() = runTest {
        initDocument()

        val valueId = "value-${uniqueId()}"
        val historyId = "history-${uniqueId()}"
        val values = listOf("A", "B", "C")

        fun getValue() = document.getElementById(valueId)?.textContent
        fun getHistory() = document.getElementById(historyId)?.textContent

        val histLength = 4

        val store = object : RootStore<String>("") {
            val hist = history<String>(histLength).sync(this)

        }

        render {
            div {
                span(id = valueId) { store.data.bind() }
                span(id = historyId) { store.hist.map { hist -> hist.joinToString() }.bind() }
            }
        }.mount(targetId)

        delay(100)
        assertEquals("", getValue())
        assertEquals("", getHistory())

        values.forEach { value ->
            store.hist.add(value)
            delay(1)
        }
        delay(200)

        assertEquals(values.reversed().joinToString(), getHistory())

        action(store.hist.back()) handledBy store.update
        delay(100)
        assertEquals(values[2], getValue())
        assertEquals(values.slice(0..1).reversed().joinToString(), getHistory())

        action(store.hist.back()) handledBy store.update
        delay(100)
        assertEquals(values[1], getValue())
        assertEquals(values[0], getHistory())

        action(store.hist.back()) handledBy store.update
        delay(100)
        assertEquals(values[0], getValue())
        assertEquals("", getHistory())
    }


}