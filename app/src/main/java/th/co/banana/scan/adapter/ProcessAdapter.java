package th.co.banana.scan.adapter;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import th.co.banana.scan.R;
import th.co.banana.scan.model.SheetDateModel;

/**
 * Created by MSILeopardPro on 15/2/2560.
 */

public class ProcessAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<SheetDateModel> dateModels = new ArrayList<>();

    public List<SheetDateModel> getDateModels() {
        return dateModels;
    }

    public void setDateModels(List<SheetDateModel> dateModels) {
        this.dateModels = dateModels;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View headerView = layoutInflater.inflate(R.layout.item_process, parent, false);
        return new ProcessHolder(headerView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ProcessHolder holder1 = (ProcessHolder) holder;

        holder1.setBm(dateModels.get(position).getBm());
        holder1.setAnswers(dateModels.get(position).getAnswers());
    }

    @Override
    public int getItemCount() {
        return dateModels.size();
    }

    public class ProcessHolder extends RecyclerView.ViewHolder {

        private ImageView bm;
        private TextView answers;

        public ProcessHolder(View itemView) {
            super(itemView);

            bm = (ImageView) itemView.findViewById(R.id.bm);
            answers = (TextView) itemView.findViewById(R.id.answers);
        }

        public void setBm(Bitmap bitmap){
            bm.setImageBitmap(bitmap);
        }

        public void setAnswers(String answers){
            this.answers.setText(answers);
        }
    }
}
