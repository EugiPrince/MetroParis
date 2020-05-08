package it.polito.tdp.metroparis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

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
		
		//System.out.println(this.graph);
		
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
		/*
		for(Fermata fp : this.fermate) {
			List<Fermata> connesse = dao.fermateSuccessive(fp, fermateIdMap);
			
			for(Fermata fa : connesse) {
				this.graph.addEdge(fp, fa);
			}
		}
		*/
		//Metodo 3: farci dare dal database direttamente gli archi che ci servono, chiedo al DB l'elenco degli archi
		//Dal punto di vista Java e' la piu' veloce, dipende dal DB se e' veloce o meno (in questo caso no problem)
		//Ho dovuto creare un oggetto in piu' pero'
		
		//UTILE SE NEL DATABASE E' AGEVOLE FARE QUESTE OPERAZIONI, IN QUESTO CASO LO ERA, MA NON SEMPRE LO E'
		
		List<CoppiaFermate> coppie = dao.coppieFermate(fermateIdMap);
		
		for(CoppiaFermate c : coppie)
			this.graph.addEdge(c.getFermataPartenza(), c.getFermataArrivo());
		
		
		//System.out.println(this.graph);
		System.out.format("Grafo creato con %d vertici e %d archi\n", this.graph.vertexSet().size(),
				this.graph.edgeSet().size());
	}
	
	/**
	 * Visita l'intero grafo con la strategia di Breadth First e ritorna l'insieme dei vertici incontrati.
	 * Quindi mi dara' la lista dei vertici raggiunti secondo una visita in ampiezza a partire dal vertice specificato
	 * @param source Vertice di partenza della visita
	 * @return l'insieme dei vertici incontrati
	 */
	public List<Fermata> visitaAmpiezza(Fermata source) { //fermata di partenza della visita
		List<Fermata> visita = new ArrayList<>();
		
		//Il costruttore ha come primo paramentro il grafo, come secondo parametro il vertice di partenza, se il
		//vertice di partenza e' null parte da un vertice a caso
		GraphIterator<Fermata, DefaultEdge> bfv = new BreadthFirstIterator<>(this.graph, source);
		
		//Aggiungo i vertici fin quando ci sono vertici ancora liberi
		while(bfv.hasNext()) {
			visita.add(bfv.next());
		}
		return visita;
	}

	public List<Fermata> visitaProfondita(Fermata source) { //fermata di partenza della visita
		List<Fermata> visita = new ArrayList<>();
		
		//Il costruttore ha come primo paramentro il grafo, come secondo parametro il vertice di partenza, se il
		//vertice di partenza e' null parte da un vertice a caso
		GraphIterator<Fermata, DefaultEdge> dfv = new DepthFirstIterator<>(this.graph, source);
		
		//Aggiungo i vertici fin quando ci sono vertici ancora liberi
		while(dfv.hasNext()) {
			visita.add(dfv.next());
		}
		return visita;
	}
	
	/**
	 * La chiave e' il vertice scoperto(nuovo), il valore e' il vertice da cui sono arrivato
	 * @param source
	 * @return
	 */
	public Map<Fermata, Fermata> alberoVisita(Fermata source) {
		Map<Fermata, Fermata> albero = new HashMap<>();
		albero.put(source, null); //La sorgente la aggiungo a mano perche' altrimenti ci sono problemi dopo
		
		GraphIterator<Fermata, DefaultEdge> bfv = new BreadthFirstIterator<>(this.graph, source);
		
		bfv.addTraversalListener(new TraversalListener<Fermata, DefaultEdge>() {
			@Override
			public void vertexTraversed(VertexTraversalEvent<Fermata> e) {}
			
			@Override
			public void vertexFinished(VertexTraversalEvent<Fermata> e) {}
			
			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultEdge> e) {
				//La visita sta considerando un arco. Questo arco ha scoperto un nuovo vertice? se si', da dove?
				DefaultEdge edge = e.getEdge();
				//Arco attraversato (a,b) ma mi devo chiedere.. ho scoperto 'a' da 'b' oppure 'b' da
				//'a'? Lo so perche' uno dei due non lo conoscevo prima -> vertice di partenza gia' l'avevo scoperto
				//in precedenza, quindi sara' il valore.. l'altro sara' la chiave (perche' non lo conoscevamo ancora)
				Fermata a = graph.getEdgeSource(edge);
				Fermata b = graph.getEdgeTarget(edge);
				
				if(albero.containsKey(a)) { //Vuol dire che ho scoperto b
					albero.put(b, a);
				}
				else {
					albero.put(a, b);
				}
			}
			
			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {}
			
			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {}
		});
		
		while(bfv.hasNext()) {
			bfv.next(); //Estrai l'elemento e ignoralo (Devo farlo perche' cosi' attraversa gli archi che mi servono)
		}
		return albero;
	}
	
	public List<Fermata> camminiMinimi(Fermata partenza, Fermata arrivo) {
		DijkstraShortestPath<Fermata, DefaultEdge> dij = new DijkstraShortestPath<>(graph);
		
		GraphPath<Fermata, DefaultEdge> cammino = dij.getPath(partenza, arrivo);
		
		return cammino.getVertexList();
	}
	
	public static void main(String args[]) {
		Model m = new Model();
		
		List<Fermata> visita1 = m.visitaAmpiezza(m.fermate.get(0));
		System.out.println(visita1+"\n"+visita1.size());
		
		List<Fermata> visita2 = m.visitaProfondita(m.fermate.get(0));
		System.out.println(visita2+"\n"+visita2.size());
		
		Map<Fermata, Fermata> albero = m.alberoVisita(m.fermate.get(0));
		for(Fermata f : albero.keySet())
			System.out.format("%s <-- %s\n", f, albero.get(f));
		
		List<Fermata> cammino = m.camminiMinimi(m.fermate.get(0), m.fermate.get(1));
		System.out.println(cammino);
	}
	
}
