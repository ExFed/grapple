package com.columnzero.grapple

import groovy.transform.*
import java.net.URLEncoder

@CompileStatic
class Graph {

    private static final URI GRAPH_URI = URI.create('grapple://graph/')
    private static final Closure<Set<Edge>> SET_CTOR = { [] as Set<Edge> }

    private final Map<Object, Object> data = [:] // (uri | [uri, uri, uri]) -> data

    private final Set<Edge> edges = [] as Set
    private final Map<Object, Set<Edge>> srIndex = [:].withDefault(SET_CTOR) // (s, r) -> [edge]
    private final Map<Object, Set<Edge>> rtIndex = [:].withDefault(SET_CTOR) // (r, t) -> [edge]
    private final Map<Object, Set<Edge>> stIndex = [:].withDefault(SET_CTOR) // (s, t) -> [edge]
    private final Map<URI, Set<Edge>> sIndex = [:].withDefault(SET_CTOR) // s -> [edge]
    private final Map<URI, Set<Edge>> rIndex = [:].withDefault(SET_CTOR) // r -> [edge]
    private final Map<URI, Set<Edge>> tIndex = [:].withDefault(SET_CTOR) // t -> [edge]

    private URI asUri(Object key) {
        def keyUri = switch (key.getClass()) {
            case URI: yield (URI) key
            case URL: yield ((URL) key).toURI()
            case Node: yield ((Node) key).uri
            case Edge: yield ((Edge) key).relationUri
            case String: yield URI.create(URLEncoder.encode((String) key) + '/')
            default: throw new IllegalArgumentException("unsupported key type: ${key.getClass()}")
        }
        this.uri.resolve(keyUri)
    }

    URI getUri() { GRAPH_URI }

    Node node(Object key) { new Node(asUri(key)) }

    Node node(Object key, Object data) {
        def n = new Node(asUri(key))
        n.data = data
        return n
    }

    Edge edge(Object s, Object r, Object t) {
        def e = new Edge(asUri(s), asUri(r), asUri(t))
        edge(e)
    }

    Edge edge(Edge e) {
        if (e !in edges) {
            edges << e
            srIndex[[e.s, e.r]] << e
            rtIndex[[e.r, e.t]] << e
            stIndex[[e.s, e.t]] << e
            sIndex[e.s] << e
            rIndex[e.r] << e
            tIndex[e.t] << e
        }
        return e
    }

    Node leftShift(Object key) { node(key) }

    Set<Edge> getRelationships(Object relationUri) { rIndex[asUri(relationUri)].asUnmodifiable() }

    @Immutable
    @ToString(includes = ['uri'])
    @EqualsAndHashCode(includes = ['uri'])
    class Node {
        URI uri

        Graph getGraph() { Graph.this }

        Object getData() { Graph.this.data[uri] }
        Object setData(Object value) { Graph.this.data[uri] = value }

        EdgeBuilder rightShift(Object r) { new EdgeBuilder(uri, r) }

        String toString() { "Node($uri)" }
        boolean equals(Object o) {
            o === this || (
                o instanceof Node
                    && this.graph === o.graph
                    && this.uri == o.uri
            )
        }
        int hashCode() { Objects.hash(Node, uri) }
    }

    @Immutable
    class Edge {
        URI sourceUri
        URI relationUri
        URI targetUri

        URI getS() { sourceUri }
        URI getR() { relationUri }
        URI getT() { targetUri }

        Node getSource() { new Node(sourceUri) }
        Node getTarget() { new Node(targetUri) }

        Graph getGraph() { Graph.this }

        Object getData() { graph.data[[s, r, t]] }
        Object setData(Object value) { graph.data[[s, r, t]] = value }

        String toString() { "Edge($s, $r, $t)" }
        boolean equals(Object o) {
            o === this || (
                o instanceof Edge
                    && this.graph === o.graph
                    && this.s == o.s
                    && this.r == o.r
                    && this.t == o.t
            )
        }
        int hashCode() { Objects.hash(Edge, s, r, t) }
    }

    @Canonical
    class EdgeBuilder {
        final Object source
        final Object relation

        Edge rightShift(Object target) { edge(source, relation, target) }
    }
}
