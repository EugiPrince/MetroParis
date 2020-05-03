package it.polito.tdp.metroparis.model;

/**
 * Classe creata per il metodo 3 di popolazione degli archi
 * @author eugenioprincipi
 *
 */
public class CoppiaFermate {

	private Fermata fermataPartenza;
	private Fermata fermataArrivo;
	
	/**
	 * @param fermataPartenza
	 * @param fermataArrivo
	 */
	public CoppiaFermate(Fermata fermataPartenza, Fermata fermataArrivo) {
		this.fermataPartenza = fermataPartenza;
		this.fermataArrivo = fermataArrivo;
	}

	public Fermata getFermataPartenza() {
		return fermataPartenza;
	}

	public void setFermataPartenza(Fermata fermataPartenza) {
		this.fermataPartenza = fermataPartenza;
	}

	public Fermata getFermataArrivo() {
		return fermataArrivo;
	}

	public void setFermataArrivo(Fermata fermataArrivo) {
		this.fermataArrivo = fermataArrivo;
	}
	
}
