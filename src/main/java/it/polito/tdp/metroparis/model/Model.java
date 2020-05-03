package it.polito.tdp.metroparis.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import it.polito.tdp.metroparis.db.MetroDAO;

public class Model {
	
	private Graph<Fermata, DefaultEdge> graph;
	private List<Fermata> fermate;
	private Map<Integer, Fermata> fermateIdMap;
	
	public Model() {
		this.graph = new SimpleDirectedGraph<>(DefaultEdge.class); //Creazione grafo vuoto, solo inizializzato
		
		// CREAZIONE DEI VERTICI
		
		MetroDAO dao = new MetroDAO();
		this.fermate = dao.getAllFermate();
		
		//inizializzo la identity map subito dopo aver tutte le fermate
		this.fermateIdMap = new HashMap<>();
		for(Fermata f : this.fermate)
			fermateIdMap.put(f.getIdFermata(), f);
		
		Graphs.addAllVertices(this.graph, this.fermate);
		System.out.println(this.graph);
		
		// CREAZIONE DEGLI ARCHI (PIU' MODI POSSIBILI)
		
		//Metodo 1: considero tutte le coppie di vertici e mi chiedo se esiste un arco tra questi due o no
		//Semplice ma poco efficiente, ci sono 619 fermate.. facendo qualche calcolo ci mettera' piu' o meno
		//5 minuti... e' semplice ma ci sono troppe query da fare 
		
		//VA BENE SU GRAFI PICCOLI
		
		/*for(Fermata fp : this.fermate)
			for(Fermata fa : this.fermate)
				if(dao.fermateConnesse(fp, fa))
					this.graph.addEdge(fa, fp);
		*/
		
		//Metodo 2: data una stazione di partenza, quali stazioni di arrivo posso raggiungere? Cosi' avro' un insieme
		//di risultati che mi dono quanti archi uscenti ci sono da una fermata e quale altra fermata posso raggiungere
		//Questo lo faccio per ogni stazione di partenza
		
		//Quindi da un vertice trovo tutti i connessi.. lavora un po' di piu' il DB e un po' meno il programma
		for(Fermata fp : this.fermate) {
			List<Fermata> connesse = dao.fermateSuccessive(fp, fermateIdMap);
			
			for(Fermata fa : connesse) {
				this.graph.addEdge(fp, fa);
			}
		}
		
		//Metodo 3: farci dare dal database direttamente gli archi che ci servono, chiedo al DB l'elenco degli archi
		//Dal punto di vista Java e' la piu' veloce, dipende dal DB se e' veloce o meno (in questo caso no problem)
		//Ho dovuto creare un oggetto in piu' pero'
		
		//UTILE SE NEL DATABASE E' AGEVOLE FARE QUESTE OPERAZIONI, IN QUESTO CASO LO ERA, MA NON SEMPRE LO E'
		
		/*
		List<CoppiaFermate> coppie = dao.coppieFermate(fermateIdMap);
		
		for(CoppiaFermate c : coppie)
			this.graph.addEdge(c.getFermataPartenza(), c.getFermataArrivo());
		*/
		
		System.out.println(this.graph);
	}

	public static void main(String args[]) {
		Model m = new Model();
	}
	
}
