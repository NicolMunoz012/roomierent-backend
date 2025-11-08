package com.roomierent.backend.util.datastructures;

import com.roomierent.backend.model.entity.Property;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Priority Queue (Min-Heap) para rankear propiedades
 *
 * Estructura de Datos: MIN-HEAP
 * Complejidad:
 * - insert(): O(log n)
 * - extractMin(): O(log n)
 * - peek(): O(1)
 *
 * Uso: Mantener las mejores N propiedades ordenadas por score de recomendación
 */
@Component
public class PropertyPriorityQueue {

    private List<PropertyScore> heap;

    public PropertyPriorityQueue() {
        this.heap = new ArrayList<>();
    }

    /**
     * Clase interna para almacenar propiedad con su score
     */
    public static class PropertyScore implements Comparable<PropertyScore> {
        private Property property;
        private double score;

        public PropertyScore(Property property, double score) {
            this.property = property;
            this.score = score;
        }

        public Property getProperty() {
            return property;
        }

        public double getScore() {
            return score;
        }

        @Override
        public int compareTo(PropertyScore other) {
            // Mayor score = mayor prioridad (invertimos para max-heap)
            return Double.compare(other.score, this.score);
        }
    }

    /**
     * Inserta una propiedad con su score en el heap
     * Complejidad: O(log n)
     */
    public void insert(Property property, double score) {
        PropertyScore ps = new PropertyScore(property, score);
        heap.add(ps);
        heapifyUp(heap.size() - 1);
    }

    /**
     * Extrae la propiedad con mayor score
     * Complejidad: O(log n)
     */
    public Property extractMax() {
        if (heap.isEmpty()) {
            return null;
        }

        PropertyScore max = heap.get(0);

        // Reemplazar con el último elemento
        PropertyScore last = heap.remove(heap.size() - 1);

        if (!heap.isEmpty()) {
            heap.set(0, last);
            heapifyDown(0);
        }

        return max.getProperty();
    }

    /**
     * Retorna la propiedad con mayor score sin eliminarla
     * Complejidad: O(1)
     */
    public Property peek() {
        return heap.isEmpty() ? null : heap.get(0).getProperty();
    }

    /**
     * Obtiene las mejores N propiedades
     */
    public List<Property> getTopN(int n) {
        List<Property> result = new ArrayList<>();
        List<PropertyScore> temp = new ArrayList<>(heap);

        for (int i = 0; i < Math.min(n, heap.size()); i++) {
            result.add(extractMax());
        }

        // Restaurar el heap
        heap = temp;

        return result;
    }

    public int size() {
        return heap.size();
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }

    public void clear() {
        heap.clear();
    }

    /**
     * Mantiene la propiedad del heap hacia arriba
     */
    private void heapifyUp(int index) {
        while (index > 0) {
            int parentIndex = (index - 1) / 2;

            if (heap.get(index).compareTo(heap.get(parentIndex)) >= 0) {
                break;
            }

            swap(index, parentIndex);
            index = parentIndex;
        }
    }

    /**
     * Mantiene la propiedad del heap hacia abajo
     */
    private void heapifyDown(int index) {
        int size = heap.size();

        while (index < size) {
            int largest = index;
            int leftChild = 2 * index + 1;
            int rightChild = 2 * index + 2;

            if (leftChild < size && heap.get(leftChild).compareTo(heap.get(largest)) < 0) {
                largest = leftChild;
            }

            if (rightChild < size && heap.get(rightChild).compareTo(heap.get(largest)) < 0) {
                largest = rightChild;
            }

            if (largest == index) {
                break;
            }

            swap(index, largest);
            index = largest;
        }
    }

    private void swap(int i, int j) {
        PropertyScore temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }
}