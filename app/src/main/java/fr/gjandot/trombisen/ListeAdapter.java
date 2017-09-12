package fr.gjandot.trombisen;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

public class ListeAdapter extends BaseAdapter  implements Filterable {
    private Context context;

    private List<Senateur> listSenateurs;
    private List<Senateur> tousSenateurs;
    private Filter mFilter;
    public ImageLoader imageLoader;

    public ListeAdapter(Context context, List<Senateur> listIn) {
        this.context = context;
        this.listSenateurs = listIn;
        this.tousSenateurs = this.listSenateurs;
        this.getFilter().filter("");
        imageLoader=new ImageLoader(context);
    }

    public int getCount() {
    	if (listSenateurs==null)
    	{
    		return 0;
    	}
    	else
    	{
    		return listSenateurs.size();
    	}
    }

    public Object getItem(int position) {
        return listSenateurs.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ImageView maVue;
        Senateur entry = listSenateurs.get(position);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.eltitem, null);
        }
        TextView tvNom = (TextView) convertView.findViewById(R.id.ELTtitre);
        tvNom.setText(entry.getNom());

        TextView tvGroupe = (TextView) convertView.findViewById(R.id.sengroupe);
        tvGroupe.setText(entry.getGrp());
        TextView tvCirco = (TextView) convertView.findViewById(R.id.sencirco);
        //tvCirco.setText(entry.getCirco());
        tvCirco.setText(entry.getLongCirco());
        maVue = (ImageView) convertView.findViewById(R.id.imageView);
        if (entry.isSexe_H())
        {
            convertView.setBackgroundColor(context.getResources().getColor(R.color.colHomme));
        }
        else
        {
            convertView.setBackgroundColor(context.getResources().getColor(R.color.colFemme));
        }
        convertView.setTag(entry.getSenUrl());

        if (maVue != null) {
            imageLoader.DisplayImage(context.getResources().getString(R.string.url_photo) + entry.getImgUrl() + "/64", maVue);
        }

        return convertView;
    }
    
    @Override
    public Filter getFilter() {
         if (mFilter == null) {
                mFilter = new FiltreParlement();
            }
        return mFilter;
    }
    
    
    private class FiltreParlement extends Filter {
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                listSenateurs = (List<Senateur>) results.values;
                notifyDataSetChanged();
            }

        @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();
                ArrayList<Senateur> filtreSenateurs = new  ArrayList<Senateur>();

            if (SenList.filtreGroupe == null)
            {
                return results;
            }

            for (int index = 0; index < tousSenateurs.size(); index++) {
                Senateur sen = tousSenateurs.get(index);
                boolean ajout = false;
                if (SenList.vueH && sen.isSexe_H()) {
                    ajout = true;
                }
                if (SenList.vueF && !sen.isSexe_H()) {
                    ajout = true;
                }
                if ((!SenList.filtreGroupe.contentEquals(context.getResources().getString(R.string.tous))) && (!sen.getGrp().contentEquals(SenList.filtreGroupe)))
                {
                    ajout=false;
                }
                if (ajout) {
                    if (constraint != null && constraint.toString().length() > 0) {
                        if (sen.getNom().charAt(0) != '-') {
                            if (sen.getNom().toUpperCase().contains(constraint.toString().toUpperCase())) {
                                filtreSenateurs.add(sen);
                            }
                        }
                    }
                    else {
                        filtreSenateurs.add(sen);
                    }
                }
            }
        results.values = filtreSenateurs;
        results.count = filtreSenateurs.size();
        return results;
        }
    }

    void clearImageCache()
    {
        imageLoader.clearCache();
    }
}

