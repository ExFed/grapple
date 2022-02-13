package com.columnzero.grapple

import org.junit.jupiter.api.*

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

class GraphTest {

    Graph g

    @BeforeEach
    void beforeEach() {
        g = new Graph()
    }

    @Test
    void addNodes() {
        def alice = g.node('alice', 'blue')
        def bob = g.node('bob', 'orange')

        assertThat(alice.data, is('blue'))
        assertThat(bob.data, is('orange'))
    }

    @Test
    void addEdges() {

        def edge = g << 'alice' >> 'knows' >> 'bob'

        def alice = g.node('alice')
        def bob = g.node('bob')

        assertThat(edge, isA(Graph.Edge))
        assertThat(edge.sourceNode, is(alice))
        assertThat(edge.targetNode, is(bob))
        assertThat(g.getRelationships('knows'), hasItem(edge))
    }
}
