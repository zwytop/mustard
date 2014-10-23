package org.data2semantics.mustard.kernels.graphkernels.rdfdata;


import java.util.List;
import java.util.Set;

import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphIntersectionPartialSubTreeKernel;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

public class RDFIntersectionPartialSubTreeKernel implements GraphKernel<RDFData> {
	private int depth;
	private String label;
	private boolean inference;
	private DTGraphIntersectionPartialSubTreeKernel kernel;
	private SingleDTGraph graph;

	public RDFIntersectionPartialSubTreeKernel(int depth, double discountFactor, boolean inference, boolean normalize) {
		super();
		this.label = "RDF_IPST_Kernel_" + depth + "_" + discountFactor + "_" + inference + "_" + normalize;
		this.depth = depth;
		this.inference = inference;

		kernel = new DTGraphIntersectionPartialSubTreeKernel(depth, discountFactor, normalize);
	}

	public String getLabel() {
		return label;
	}

	public void setNormalize(boolean normalize) {
		kernel.setNormalize(normalize);
	}

	public double[][] compute(RDFData data) {
		init(data.getDataset(), data.getInstances(), data.getBlackList());
		return kernel.compute(graph);
	}

	private void init(RDFDataSet dataset, List<Resource> instances, List<Statement> blackList) {
		Set<Statement> stmts = RDFUtils.getStatements4Depth(dataset, instances, depth, inference);
		stmts.removeAll(blackList);
		graph = RDFUtils.statements2Graph(stmts, RDFUtils.REGULAR_LITERALS, instances, false);
	}	
}
