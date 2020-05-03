package it.polito.tdp.metroparis.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.javadocmd.simplelatlng.LatLng;

import it.polito.tdp.metroparis.model.CoppiaFermate;
import it.polito.tdp.metroparis.model.Fermata;
import it.polito.tdp.metroparis.model.Linea;

public class MetroDAO {

	public List<Fermata> getAllFermate() {

		final String sql = "SELECT id_fermata, nome, coordx, coordy FROM fermata ORDER BY nome ASC";
		List<Fermata> fermate = new ArrayList<Fermata>();

		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				Fermata f = new Fermata(rs.getInt("id_Fermata"), rs.getString("nome"),
						new LatLng(rs.getDouble("coordx"), rs.getDouble("coordy")));
				fermate.add(f);
			}

			st.close();
			conn.close();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Errore di connessione al Database.");
		}

		return fermate;
	}

	public List<Linea> getAllLinee() {
		final String sql = "SELECT id_linea, nome, velocita, intervallo FROM linea ORDER BY nome ASC";

		List<Linea> linee = new ArrayList<Linea>();

		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				Linea f = new Linea(rs.getInt("id_linea"), rs.getString("nome"), rs.getDouble("velocita"),
						rs.getDouble("intervallo"));
				linee.add(f);
			}

			st.close();
			conn.close();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Errore di connessione al Database.");
		}

		return linee;
	}

	//METODO 1	
	public boolean fermateConnesse(Fermata fp, Fermata fa) {
		
		String sql = "SELECT COUNT(*) AS C FROM connessione WHERE id_stazP = ? AND id_stazA = ?";
		
		try {
		Connection conn = DBConnect.getConnection();
		PreparedStatement st = conn.prepareStatement(sql);
		
		st.setInt(1, fp.getIdFermata());
		st.setInt(2, fa.getIdFermata());
		
		ResultSet rs = st.executeQuery();
		
		rs.first();
		int numeroLinee = rs.getInt("C");
		
		conn.close();
		return numeroLinee>=1;
		
		} catch(SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Errore di connessione al Database.");
		}
	}
	
	//METODO 2
	public List<Fermata> fermateSuccessive(Fermata fp, Map<Integer, Fermata> idMap) {
		
		String sql = "SELECT DISTINCT id_stazA FROM connessione WHERE id_stazP = ?";
		List<Fermata> result = new ArrayList<>();
		
		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			
			st.setInt(1, fp.getIdFermata());
			
			ResultSet rs = st.executeQuery();
			
			while(rs.next()) {
				int id_fa = rs.getInt("id_stazA"); //ID fermata di arrivo.. devo trovare l'oggetto fermata corrispondente
				//ma qui nel dao c'e' gia' la lista di tutte le fermate.. lo trovo da li'
				result.add(idMap.get(id_fa));
			}
			conn.close();
			
			return result;
		} catch(SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Errore di connessione al Database.");
		}
	}

	//METODO 3
	public List<CoppiaFermate> coppieFermate(Map<Integer, Fermata> idMap) {
		String sql = "SELECT DISTINCT id_stazP, id_stazA FROM connessione";
		List<CoppiaFermate> coppie = new ArrayList<>();
		
		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			
			ResultSet rs = st.executeQuery();
			
			while(rs.next()) {
				CoppiaFermate c = new CoppiaFermate(idMap.get(rs.getInt("id_stazP")), idMap.get(rs.getObject("id_stazA")));
				coppie.add(c);
			}
			
			conn.close();
		} catch(SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Errore di connessione al Database.");
		}
		return coppie;
	}
	
}
