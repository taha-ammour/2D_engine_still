package org.example.engine.core;

import org.example.engine.rendering.Renderable;

import java.util.Comparator;

/**
 * Comparator for sorting renderables by Z-order.
 */
public class ZComparator implements Comparator<Renderable> {
    @Override
    public int compare(Renderable a, Renderable b) {
        return Float.compare(a.getZ(), b.getZ());
    }
}