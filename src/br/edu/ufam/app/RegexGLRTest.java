package br.edu.ufam.app;

import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.edu.ufam.model.ConsensusPattern;
import br.edu.ufam.model.DistanceMatrix;
import br.edu.ufam.utility.Utility;

public class RegexGLRTest {
	
	private ConsensusPattern consensusPatterns[] = null;
	
	@Before
	public void setUp() throws Exception {
		consensusPatterns = null;
		
		ConsensusPattern finalConsensusPattern = null;
		ArrayList<String> attributeValues = Utility.readFile("redatasetsexpressaoregular/cameras.txt");
		
		consensusPatterns = new ConsensusPattern[attributeValues.size()];
		
		for (int i = 0; i < consensusPatterns.length; ++i) {
			
			consensusPatterns[i] = new ConsensusPattern(attributeValues.get(i));
			
		}
		
		DistanceMatrix distanceMatrix = new DistanceMatrix(consensusPatterns);
		
	}

	@After
	public void tearDown() throws Exception {
		consensusPatterns = null;
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}

}
