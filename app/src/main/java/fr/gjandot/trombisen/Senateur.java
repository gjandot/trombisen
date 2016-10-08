package fr.gjandot.trombisen;

import java.util.Comparator;

public class Senateur {
	private String nom;
	private String prenom;
	private String imgurl;
	private String senurl;
	private String circo;
	private String grp;
	private boolean sexe_H = true;


	public Senateur(Senateur sen)
	{
		super();
		this.nom = sen.nom;
		this.imgurl = sen.imgurl;
		this.senurl = sen.senurl;
		this.grp = sen.grp;
		this.sexe_H = sen.sexe_H;
		this.circo = sen.circo;
	}

	public Senateur()
	{
		super();
	}

	public String getNom() {
		return nom;
	}
	public void setNom(String nom) { this.nom = nom; }

	public String getImgUrl() {
		return this.imgurl;
	}
	public void setImgUrl(String imgurl) { this.imgurl = imgurl; }

	public String getSenUrl() {
		return this.senurl;
	}
	public void setSenUrl(String senurl) { this.senurl = senurl; }

	public String getGrp() {
		return this.grp;
	}
	public void setGrp(String grp) { this.grp = grp; }

	public String getCirco() {
		return this.circo;
	}
	public void setCirco(String circo) { this.circo = circo; }

	public void setSexe_H(boolean sexe_h) { this.sexe_H = sexe_h;}
	public boolean isSexe_H() { return this.sexe_H; }

	static class SenComparateur implements Comparator<Object> {
		    public int compare(Object o1, Object o2) {
		      if (!(o1 instanceof Senateur) || !(o2 instanceof Senateur))
		        throw new ClassCastException();

				Senateur s1 = (Senateur) o1;
				Senateur s2 = (Senateur) o2;

		      return s1.getNom().compareTo(s2.getNom());
		    }
	}
}
