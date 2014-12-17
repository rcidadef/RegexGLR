package br.edu.ufam;

import java.util.*;

public class DistanceMatrix {
	private int imax;
	private int jmax;
	private ArrayList<ConsensusPattern> l;
	private ArrayList<ArrayList<Integer>> s;

	DistanceMatrix(ArrayList<ConsensusPattern> list) {
		l = list;
		s = new ArrayList<ArrayList<Integer>>(l.size());
		// Inicializa matrix de distancias
		for (int i = 0; i < l.size(); ++i) {
			s.add(new ArrayList<Integer>(l.size()));
			for (int j = 0; j < l.size(); ++j) {
				s.get(i).add(null);
			}
		}
	}

	// Retorna a maior pontuacao na matrix de distancias e coloca nos indices
	// imax e jmax a posicao na matrix desse valor, que corresponde tambem a
	// posicao dos ConsensusPattern em l com o maior score
	private int getMaxScoreIndexes(ArrayList<ArrayList<Integer>> list) {
		int max = list.get(0).get(0).intValue();
		imax = 0;
		jmax = 1;
		for (int i = 0; i < list.size(); ++i) {
			for (int j = 0; j < list.size(); ++j) {
				int value = list.get(i).get(j).intValue();
				if (value > max) {
					max = value;
					imax = i;
					jmax = j;
				}
			}
		}
		return max;
	}

	// Combina todos os ConsensusPattern e retorna o final
	public ConsensusPattern getConsensusPattern() {
		int ip;
		ConsensusPattern p = l.get(0);
		PathMatrix pm = new PathMatrix();
		// Preenche a matrix de distancias com a pontuacao de cada par de 
		// ConsensusPattern em l
		int size = l.size();
		for (int i = 0; i < size; ++i) {
			s.get(i).set(i, new Integer(0));
			for (int j = i + 1; j < size; ++j) {
				pm.buildMatrix(l.get(i), l.get(j));
				s.get(i).set(j, new Integer(pm.getScore()));
				s.get(j).set(i, new Integer(pm.getScore()));
			}
		}

		// Combina cada par de ConsensusPattern com a maior pontuacao e os
		// remove de l e adiciona o resultado em l, ate que haja apenas um
		// ConsensusPattern
		while (l.size() > 1) {
			// Pegar indices de maior pontuacao
			getMaxScoreIndexes(s);
			// Constroi a matrix de caminhamento, alinha ConsensusPattern e 
			// os une
			pm.buildMatrix(l.get(imax), l.get(jmax));
			pm.align();
			try {
				p = pm.merge();
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Adiciona ConsensusPattern gerado a l
			l.add(p);

			// Remove os ConsensusPattern usados para gerar p de l
			l.remove(imax);
			// Remove as linhas  de s correspondentes aos ConsensusPattern 
			// usados para gerar p
			s.remove(imax);
			if (jmax >= imax) {
				jmax--;
			}
			l.remove(jmax);
			s.remove(jmax);
			// Remove as colunas de s correspondentes aos ConsensusPattern
			// usados para gerar p  
			for (ArrayList<Integer> ai : s) {
				ai.remove(imax);
				ai.remove(jmax);
			}

			// Adiciona nova linha e uma nova coluna para cada linha em s
			// correspondendo ao novo ConsensusPattern adicionado em l
			ip = l.size() - 1;
			s.add(new ArrayList<Integer>(l.size() + 1));
			for (int i = 0; i < ip; ++i) {
				s.get(ip).add(null);
			}
			s.get(ip).add(ip, new Integer(0));

			// Calcula a pontuacao de cada ConsensusPattern em l para o novo
			// e o poe em s
			int length = s.size() - 1;
			for (int i = 0; i < length; ++i) {
				pm.buildMatrix(l.get(ip), l.get(i));
				s.get(ip).set(i, new Integer(pm.getScore()));
				s.get(i).add(new Integer(pm.getScore()));
			}
//			// Remove os ConsensusPattern usados para gerar p de l
//			l.remove(imax);
//			if (jmax < imax) {
//				l.remove(jmax);
//			} else {
//				l.remove(jmax - 1);
//			}
//
//			// Remove as linhas  de s correspondentes aos ConsensusPattern 
//			// usados para gerar p
//			s.remove(imax);
//			if (jmax < imax) {
//				s.remove(jmax);
//			} else {
//				s.remove(jmax - 1);
//			}
//
//			// Remove as colunas de s correspondentes aos ConsensusPattern
//			// usados para gerar p  
//			for (ArrayList<Integer> ai : s) {
//				ai.remove(imax);
//				if (jmax < imax) {
//					ai.remove(jmax);
//				} else {
//					ai.remove(jmax - 1);
//				}
//			}
//
//			// Adiciona nova linha e uma nova coluna para cada linha em s
//			// correspondendo ao novo ConsensusPattern adicionado em l
//			ip = l.size()-1;
//			s.add(new ArrayList<Integer>());
//			for (int i = 0; i < l.size(); ++i) {
//				s.get(ip).add(new Integer(0));
//			}
//
//			// Calcula a pontuacao de cada ConsensusPattern em l para o novo
//			// e o poe em s
//			for (int i = 0; i < ip; ++i) {
//				pm.buildMatrix(l.get(ip), l.get(i));
//				s.get(ip).add(i, new Integer(pm.getScore()));
//				s.get(i).add(ip, new Integer(pm.getScore()));
//			}
		}

		return p;
	}
}