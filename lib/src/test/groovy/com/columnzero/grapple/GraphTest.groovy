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

        def akb = g << 'alice' >> 'knows' >> 'bob'
        akb.data = 'best friends'

        def bka = g << 'bob' >> 'knows' >> 'alice'
        bka.data = 'coworkers'

        def alice = g.node('alice')
        def bob = g.node('bob')
        def knowsEdges = g.getRelationships('knows')

        assertThat(akb, isA(Graph.Edge))
        assertThat(akb.source, is(alice))
        assertThat(akb.target, is(bob))
        assertThat(knowsEdges, hasItems(akb, bka))
        assertThat(knowsEdges*.data, hasItems('best friends', 'coworkers'))
    }
}
