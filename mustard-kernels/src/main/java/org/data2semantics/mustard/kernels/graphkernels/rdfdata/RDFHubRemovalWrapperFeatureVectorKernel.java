package org.data2semantics.mustard.kernels.graphkernels.rdfdata;

import java.util.List;
import java.util.Set;

import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.SparseVector;
import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphHubRemovalWrapperFeatureVectorKernel;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

/**
 * Wrapper kernel for {@link org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphHubRemovalWrapperFeatureVectorKernel}. 
 * 
 * @author Gerben
 *
 * @param <K>
 */
public class RDFHubRemovalWrapperFeatureVectorKernel<K extends FeatureVectorKernel<SingleDTGraph>> implements GraphKernel<RDFData>, FeatureVectorKernel<RDFData> {
	private int depth;
	private boolean inference;
	private DTGraphHubRemovalWrapperFeatureVectorKernel<K> kernel2;
	private SingleDTGraph graph;
	
	public RDFHubRemovalWrapperFeatureVectorKernel(K kernel, int depth, boolean inference, int[] minHubSizes, boolean normalize) {
		super();
		this.depth = depth;
		this.inference = inference;
		kernel2 = new DTGraphHubRemovalWrapperFeatureVectorKernel<K>(kernel, minHubSizes, normalize);
	}

	public RDFHubRemovalWrapperFeatureVectorKernel(K kernel, int depth, boolean inference, int minHubSize, boolean normalize) {
		super();
		this.depth = depth;
		this.inference = inference;
		kernel2 = new DTGraphHubRemovalWrapperFeatureVectorKernel<K>(kernel, minHubSize, normalize);
	}

	public String getLabel() {
		return KernelUtils.createLabel(this) + "_" + kernel2.getLabel();		
	}

	public void setNormalize(boolean normalize) {
		kernel2.setNormalize(normalize);
	}

	public SparseVector[] computeFeatureVectors(RDFData data) {
		init(data.getDataset(), data.getInstances(), data.getBlackList());
		return kernel2.computeFeatureVectors(graph);
	}

	public double[][] compute(RDFData data) {
		init(data.getDataset(), data.getInstances(), data.getBlackList());
		return kernel2.compute(graph);
	}

	private void init(RDFDataSet dataset, List<Resource> instances, List<Statement> blackList) {
		Set<Statement> stmts = RDFUtils.getStatements4Depth(dataset, instances, depth, inference);
		stmts.removeAll(blackList);
		graph = RDFUtils.statements2Graph(stmts, RDFUtils.REGULAR_LITERALS, instances, true);
	}
}
