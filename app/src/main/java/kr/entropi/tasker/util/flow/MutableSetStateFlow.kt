package kr.entropi.tasker.util.flow

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet

open class MutableSetStateFlow<T>(
    private val flow: MutableStateFlow<Set<T>>,
    private val onAdd: (T) -> Unit = {},
    private val onRemove: (T) -> Unit = {}
) :
    StateFlow<Set<T>> by flow, MutableSet<T>, RandomAccess {

    override val size: Int
        get() = flow.value.size

    override fun isEmpty(): Boolean {
        return flow.value.isEmpty()
    }

    override fun contains(element: T): Boolean {
        return flow.value.contains(element)
    }

    override fun iterator(): MutableIterator<T> {
        return MutableIteratorImpl()
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        return flow.value.containsAll(elements)
    }

    override fun add(element: T): Boolean {
        onAdd(element)
        flow.update { it + element }
        return true
    }

    override fun remove(element: T): Boolean {
        onRemove(element)
        flow.update { it - element }
        return true
    }

    override fun addAll(elements: Collection<T>): Boolean {
        elements.forEach {
            if (it !in flow.value) onAdd(it)
        }
        flow.update { it + elements }
        return true
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        elements.forEach {
            if (it in flow.value) onRemove(it)
        }
        flow.update { it - elements }
        return true
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        flow.value.forEach {
            if (it !in elements) onRemove(it)
        }
        flow.updateAndGet { it.filter { it in elements }.toSet() }
        return true
    }

    override fun clear() {
        flow.value.forEach(onRemove)
        flow.update { emptySet() }
    }



    private inner class MutableIteratorImpl : MutableIterator<T> {
        var iterator = flow.value.iterator()
        var last: T? = null
        var lastRemoved = false

        override fun next(): T {
            last = iterator.next()
            lastRemoved = false
            return last!!
        }

        override fun hasNext(): Boolean {
            return iterator.hasNext()
        }

        override fun remove() {
            if (last == null) {
                throw IllegalStateException("next has not been called yet")
            }
            if (lastRemoved) {
                throw IllegalStateException("remove has already been called")
            }

            remove(last)
            lastRemoved = true
        }
    }

}