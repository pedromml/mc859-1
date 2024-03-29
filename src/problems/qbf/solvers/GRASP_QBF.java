package problems.qbf.solvers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import metaheuristics.grasp.AbstractGRASP;
import problems.qbf.QBF_Inverse;
import solutions.Solution;



/**
 * Metaheuristic GRASP (Greedy Randomized Adaptive Search Procedure) for
 * obtaining an optimal solution to a QBF (Quadractive Binary Function --
 * {@link #QuadracticBinaryFunction}). Since by default this GRASP considers
 * minimization problems, an inverse QBF function is adopted.
 * 
 * @author ccavellucci, fusberti
 */
public class GRASP_QBF extends AbstractGRASP<Integer> {

	/**
	 * Constructor for the GRASP_QBF class. An inverse QBF objective function is
	 * passed as argument for the superclass constructor.
	 * 
	 * @param alpha
	 *            The GRASP greediness-randomness parameter (within the range
	 *            [0,1])
	 * @param iterations
	 *            The number of iterations which the GRASP will be executed.
	 * @param filename
	 *            Name of the file for which the objective function parameters
	 *            should be read.
	 * @throws IOException
	 *             necessary for I/O operations.
	 */
	public GRASP_QBF(Double alpha, Integer searchMethod, Integer constHeu, Integer p, Integer iterations, String filename) throws IOException {
		super(new QBF_Inverse(filename), alpha, searchMethod, constHeu, p, iterations);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see grasp.abstracts.AbstractGRASP#makeCL()
	 */
	@Override
	public ArrayList<Integer> makeCL() {

		ArrayList<Integer> _CL = new ArrayList<Integer>();
		for (int i = 0; i < ObjFunction.getDomainSize(); i++) {
			Integer cand = i;
			_CL.add(cand);
		}

		return _CL;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see grasp.abstracts.AbstractGRASP#makeRCL()
	 */
	@Override
	public ArrayList<Integer> makeRCL() {

		ArrayList<Integer> _RCL = new ArrayList<Integer>();

		return _RCL;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see grasp.abstracts.AbstractGRASP#updateCL()
	 */
	@Override
	public void updateCL() {

		// do nothing since all elements off the solution are viable candidates.

	}

	/**
	 * {@inheritDoc}
	 * 
	 * This createEmptySol instantiates an empty solution and it attributes a
	 * zero cost, since it is known that a QBF solution with all variables set
	 * to zero has also zero cost.
	 */
	@Override
	public Solution<Integer> createEmptySol() {
		Solution<Integer> sol = new Solution<Integer>();
		sol.cost = 0.0;
		return sol;
	}
	
	/**
	 * {@inheritDoc}
	 * First-improving
	 * The local search operator developed for the QBF objective function is
	 * composed by the neighborhood moves Insertion, Removal and 2-Exchange.
	 */
	@Override
	public Solution<Integer> localSearchFirstImproving() {

		Double minDeltaCost;
		Integer bestCandIn = null, bestCandOut = null;

		do {
			minDeltaCost = Double.POSITIVE_INFINITY;
			updateCL();
				
			
			// Evaluate exchanges
			for (Integer candIn : CL) {
				for (Integer candOut : sol) {
					double deltaCost = ObjFunction.evaluateExchangeCost(candIn, candOut, sol);
					boolean canAddElement = ObjFunction.evaluateWeightCapacityExchange(candIn, candOut, sol);
					if (deltaCost < minDeltaCost && canAddElement) {
						minDeltaCost = deltaCost;
						bestCandIn = candIn;
						bestCandOut = candOut;
						sol.add(bestCandIn);
						CL.remove(bestCandIn);
						sol.remove(bestCandOut);
						CL.add(bestCandOut);
						return null;
					}
				}
			}
			
			// Evaluate removals
			for (Integer candOut : sol) {
				double deltaCost = ObjFunction.evaluateRemovalCost(candOut, sol);
				boolean canAddElement = ObjFunction.evaluateWeightCapacityRemove(candOut, sol);
				if (deltaCost < minDeltaCost && canAddElement) {
					minDeltaCost = deltaCost;
					bestCandIn = null;
					bestCandOut = candOut;
					sol.remove(bestCandOut);
					CL.add(bestCandOut);
					return null;
				}
			}
		} while (minDeltaCost < -Double.MIN_VALUE);

		return null;
	}

	/**
	 * {@inheritDoc}
	 * Best-improving
	 * The local search operator developed for the QBF objective function is
	 * composed by the neighborhood moves Insertion, Removal and 2-Exchange.
	 */
	@Override
	public Solution<Integer> localSearchBestImproving() {

		Double minDeltaCost;
		Integer bestCandIn = null, bestCandOut = null;

		do {
			minDeltaCost = Double.POSITIVE_INFINITY;
			updateCL();
				
			// Evaluate insertions
			for (Integer candIn : CL) {
				double deltaCost = ObjFunction.evaluateInsertionCost(candIn, sol);
				boolean canAddElement = ObjFunction.evaluateWeightCapacity(candIn, sol);
				if (deltaCost < minDeltaCost && canAddElement) {
					minDeltaCost = deltaCost;
					bestCandIn = candIn;
					bestCandOut = null;
				}
			}
			// Evaluate removals
			for (Integer candOut : sol) {
				double deltaCost = ObjFunction.evaluateRemovalCost(candOut, sol);
				boolean canAddElement = ObjFunction.evaluateWeightCapacityRemove(candOut, sol);
				if (deltaCost < minDeltaCost && canAddElement) {
					minDeltaCost = deltaCost;
					bestCandIn = null;
					bestCandOut = candOut;
				}
			}
			// Evaluate exchanges
			for (Integer candIn : CL) {
				for (Integer candOut : sol) {
					double deltaCost = ObjFunction.evaluateExchangeCost(candIn, candOut, sol);
					boolean canAddElement = ObjFunction.evaluateWeightCapacityExchange(candIn, candOut, sol);
					if (deltaCost < minDeltaCost && canAddElement) {
						minDeltaCost = deltaCost;
						bestCandIn = candIn;
						bestCandOut = candOut;
					}
				}
			}
			// Implement the best move, if it reduces the solution cost.
			if (minDeltaCost < -Double.MIN_VALUE) {
				if (bestCandOut != null) {
					sol.remove(bestCandOut);
					CL.add(bestCandOut);
				}
				if (bestCandIn != null) {
					sol.add(bestCandIn);
					CL.remove(bestCandIn);
				}
				ObjFunction.evaluate(sol);
			}
		} while (minDeltaCost < -Double.MIN_VALUE);

		return null;
	}

	/**
	 * A main method used for testing the GRASP metaheuristic.
	 * @throws Exception 
	 * 
	 */
	public static void main(String[] args) throws Exception {
		String instanceFile = args[0];
		Integer iterations = Integer.valueOf(args[1]);
		Double alpha = Double.valueOf(args[2]);
		Integer searchMethod = Integer.valueOf(args[3]);
		Integer constrHeu = Integer.valueOf(args[4]);
		Integer p = 0;
		if(constrHeu == 2) {
			p = Integer.valueOf(args[4]);
		}
		
		if(alpha < 0 || alpha > 1 || searchMethod <= 0 || searchMethod > 2) {
			throw new Exception("Invalid arguments");
		}
		
		long startTime = System.currentTimeMillis();
		GRASP_QBF grasp = new GRASP_QBF(alpha, searchMethod, constrHeu, p, iterations, "instances/kqbf/kqbf" + instanceFile);
		Solution<Integer> bestSol = grasp.solve();
		System.out.println("maxVal = " + bestSol);
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Time = "+(double)totalTime/(double)1000+" seg");
		
		String outputFileName = "";
		for(String s : args) { 
			outputFileName = outputFileName + s + "_";
			
		}
		PrintWriter writer = new PrintWriter("output_kqbf" + instanceFile + outputFileName, "UTF-8");
		writer.printf(outputFileName);
		writer.println();
		writer.println("maxVal = " + bestSol);
		writer.println("Time = "+(double)totalTime/(double)1000+" seg");
		writer.close();

	}

}
