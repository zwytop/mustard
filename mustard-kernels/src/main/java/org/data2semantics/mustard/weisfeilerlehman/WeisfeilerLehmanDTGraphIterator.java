package org.data2semantics.mustard.weisfeilerlehman;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;

public class WeisfeilerLehmanDTGraphIterator extends WeisfeilerLehmanIterator<DTGraph<StringLabel,StringLabel>> {
	private boolean reverse;
	private boolean trackPrevNBH;

	public WeisfeilerLehmanDTGraphIterator(boolean reverse) {
		this(reverse, false);
	}

	public WeisfeilerLehmanDTGraphIterator(boolean reverse, boolean trackPrevNBH) {
		super();
		this.reverse = reverse;
		this.trackPrevNBH = trackPrevNBH;
	}

	@Override
	public void wlInitialize(List<DTGraph<StringLabel, StringLabel>> graphs) {
		for (DTGraph<StringLabel, StringLabel> graph : graphs) {
			for (DTNode<StringLabel,StringLabel> node : graph.nodes()) {
				String lab = labelDict.get(node.label().toString());
				if (lab == null) {
					lab = Integer.toString(labelDict.size());
					labelDict.put(node.label().toString(), lab);
				}
				node.label().clear();
				node.label().append(lab);
				
				if (trackPrevNBH) {
					node.label().setPrevNBH("");
					node.label().setSameAsPrev(false);
				}
				
			}
			for (DTLink<StringLabel,StringLabel> link : graph.links()) {
				String lab = labelDict.get(link.tag().toString());
				if (lab == null) {
					lab = Integer.toString(labelDict.size());
					labelDict.put(link.tag().toString(), lab);
				}
				link.tag().clear();
				link.tag().append(lab);
				
				if (trackPrevNBH) {
					link.tag().setPrevNBH("");
					link.tag().setSameAsPrev(false);
				}
			}
		}
	}

	@Override
	public void wlIterate(List<DTGraph<StringLabel, StringLabel>> graphs) {
		Map<String, Bucket<DTNode<StringLabel,StringLabel>>> bucketsV = new HashMap<String, Bucket<DTNode<StringLabel,StringLabel>>>();
		Map<String, Bucket<DTLink<StringLabel,StringLabel>>> bucketsE = new HashMap<String, Bucket<DTLink<StringLabel,StringLabel>>>();

		// 1. Fill buckets 
		if (reverse) { // Labels "travel" in the root direction	
			for (DTGraph<StringLabel,StringLabel> graph : graphs) {
				// Add each edge source (i.e.) start vertex to the bucket of the edge label
				for (DTLink<StringLabel,StringLabel> edge : graph.links()) {
					if (!bucketsV.containsKey(edge.tag().toString())) {
						bucketsV.put(edge.tag().toString(), new Bucket<DTNode<StringLabel,StringLabel>>(edge.tag().toString()));
					}			
					bucketsV.get(edge.tag().toString()).getContents().add(edge.from());
				}

				// Add each incident edge to the bucket of the node label
				for (DTNode<StringLabel,StringLabel> vertex : graph.nodes()) {
					if (!bucketsE.containsKey(vertex.label().toString())) {
						bucketsE.put(vertex.label().toString(), new Bucket<DTLink<StringLabel,StringLabel>>(vertex.label().toString()));
					}
					bucketsE.get(vertex.label().toString()).getContents().addAll(vertex.linksIn());
				}	
			}
		} else { // Labels "travel" in the fringe vertices direction
			for (DTGraph<StringLabel,StringLabel> graph : graphs) {
				// Add each edge source (i.e.) start vertex to the bucket of the edge label
				for (DTLink<StringLabel,StringLabel> edge : graph.links()) {
					if (!bucketsV.containsKey(edge.tag().toString())) {
						bucketsV.put(edge.tag().toString(), new Bucket<DTNode<StringLabel,StringLabel>>(edge.tag().toString()));
					}
					bucketsV.get(edge.tag().toString()).getContents().add(edge.to());
				}

				// Add each incident edge to the bucket of the node label
				for (DTNode<StringLabel,StringLabel> vertex : graph.nodes()) {
					if (!bucketsE.containsKey(vertex.label().toString())) {
						bucketsE.put(vertex.label().toString(), new Bucket<DTLink<StringLabel,StringLabel>>(vertex.label().toString()));
					}
					bucketsE.get(vertex.label().toString()).getContents().addAll(vertex.linksOut());
				}	
			}
		}


		// Since the labels are not necessarily neatly from i to n+i, we sort them
		List<String> keysE = new ArrayList<String>(bucketsE.keySet());
		Collections.sort(keysE);
		List<String> keysV = new ArrayList<String>(bucketsV.keySet());
		Collections.sort(keysV);

		/*
		for (DTGraph<StringLabel,StringLabel> graph : graphs) {
			for (DTNode<StringLabel,StringLabel> node : graph.nodes()) {
				node.label().clear();
			}
		}
		*/
		

		// 3. Relabel to the labels in the buckets
		for (String key : keysV) {	
			// Process vertices
			Bucket<DTNode<StringLabel,StringLabel>> bucketV = bucketsV.get(key);			
			for (DTNode<StringLabel,StringLabel> vertex : bucketV.getContents()) {
				vertex.label().append("_");
				vertex.label().append(bucketV.getLabel());				
			}
		}
		for (String key : keysE) {
			// Process edges
			Bucket<DTLink<StringLabel,StringLabel>> bucketE = bucketsE.get(key);			
			for (DTLink<StringLabel,StringLabel> edge : bucketE.getContents()) {
				edge.tag().append("_");
				edge.tag().append(bucketE.getLabel());
			}
		}

		String label;
		for (DTGraph<StringLabel,StringLabel> graph : graphs) {
			for (DTLink<StringLabel,StringLabel> edge : graph.links()) {
				if (trackPrevNBH) {
					String nb = edge.tag().toString();
					nb = nb.substring(nb.indexOf("_"));

					if (nb.equals(edge.tag().getPrevNBH())) {
						edge.tag().setSameAsPrev(true);
					}
					edge.tag().setPrevNBH(nb);
				}

				if (!edge.tag().isSameAsPrev()) {
					label = labelDict.get(edge.tag().toString());						
					if (label == null) {					
						label = Integer.toString(labelDict.size());
						labelDict.put(edge.tag().toString(), label);				
					}
					edge.tag().clear();
					edge.tag().append(label);
				} else { // retain old label
					String old = edge.tag().toString();
					if (old.contains("_")) {
						old = old.substring(0, old.indexOf("_"));
						edge.tag().clear();
						edge.tag().append(old);
					} 
				}
			}

			for (DTNode<StringLabel,StringLabel> vertex : graph.nodes()) {
				if (trackPrevNBH) {
					String nb = vertex.label().toString();
					if (nb.contains("_")) {
						nb = nb.substring(nb.indexOf("_"));
					} else {
						nb = "";
					}

					if (nb.equals(vertex.label().getPrevNBH())) {
						vertex.label().setSameAsPrev(true);
					}				
					vertex.label().setPrevNBH(nb);
				}

				if (!vertex.label().isSameAsPrev()) {
					label = labelDict.get(vertex.label().toString());
					if (label == null) {
						label = Integer.toString(labelDict.size());
						labelDict.put(vertex.label().toString(), label);
					}
					vertex.label().clear();
					vertex.label().append(label);
				} else { // retain old label
					String old = vertex.label().toString();
					if (old.contains("_")) {
						old = old.substring(0, old.indexOf("_"));
						vertex.label().clear();
						vertex.label().append(old);
					} 
				}
			}
		}
	}
}
