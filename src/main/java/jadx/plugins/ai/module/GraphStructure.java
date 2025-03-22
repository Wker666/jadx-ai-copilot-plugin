package jadx.plugins.ai.module;

import jadx.api.JavaNode;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class GraphStructure {
	public static class Edge {
		private final JavaNode source;
		private final JavaNode target;

		public Edge(JavaNode source, JavaNode target) {
			this.source = source;
			this.target = target;
		}

		public JavaNode getSource() {
			return source;
		}

		public JavaNode getTarget() {
			return target;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null || getClass() != obj.getClass()) return false;
			Edge edge = (Edge) obj;
			return Objects.equals(source, edge.source) && Objects.equals(target, edge.target);
		}

		@Override
		public int hashCode() {
			return Objects.hash(source, target);
		}
	}
	private Set<JavaNode> nodes;
	private Set<Edge> edges;
	private JavaNode mainNode;

	public GraphStructure(JavaNode mainNode) {
		nodes = new HashSet<>();
		edges = new HashSet<>();
		this.mainNode = mainNode;
		nodes.add(mainNode);
	}

	public JavaNode getMainNode() {
		return mainNode;
	}

	public Set<JavaNode> getNodes() {
		return nodes;
	}

	public Set<Edge> getEdges() {
		return edges;
	}

	public void addNode(JavaNode node) {
		nodes.add( node);
	}

	/**
	 * node -> to
	 * @param node
	 * @param to
	 */
	public void addNodeEdgeTaget(JavaNode node, JavaNode to) {
		nodes.add(node);
		addEdge(node,to);
	}

	/**
	 * from -> node
	 * @param node
	 * @param from
	 */
	public void addNodeEdgeFrom(JavaNode node, JavaNode from) {
		nodes.add(node);
		addEdge(from,node);
	}

	public void addEdge(JavaNode from, JavaNode to) {
		edges.add(new Edge(from, to));
	}
}
