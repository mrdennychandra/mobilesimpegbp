package id.go.bpkp.appsimpegbpkp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

import id.go.bpkp.appsimpegbpkp.FormKaryawanActivity;
import id.go.bpkp.appsimpegbpkp.R;
import id.go.bpkp.appsimpegbpkp.http.ApiInterface;
import id.go.bpkp.appsimpegbpkp.http.Karyawan;
import id.go.bpkp.appsimpegbpkp.http.RestClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static id.go.bpkp.appsimpegbpkp.http.RestClient.BASE_IMAGE;

public class KaryawanAdapter extends RecyclerView.Adapter<KaryawanAdapter.ViewHolder> {

    Context context;
    List<Karyawan> karyawans;
    ApiInterface api;

    public KaryawanAdapter(Context context, List<Karyawan> karyawans) {
        this.context = context;
        this.karyawans = karyawans;
        api = RestClient.getClient().create(ApiInterface.class);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_karyawan, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final Karyawan karyawan = karyawans.get(i);
        viewHolder.txtNama.setText(karyawan.nama);
        viewHolder.txtEmail.setText(karyawan.email);
        viewHolder.txtTgllahir.setText(karyawan.tgllahir);
        viewHolder.txtTelpon.setText(karyawan.telpon);
        viewHolder.txtJk.setText(karyawan.jeniskelamin);
        Glide.with(context).load(BASE_IMAGE + karyawan.foto).into(viewHolder.imgFoto);
        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,FormKaryawanActivity.class);
                intent.putExtra("karyawan",karyawan);
                context.startActivity(intent);
            }
        });
        viewHolder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Konfirmasi");
                builder.setMessage("Hapus data?");
                builder.setPositiveButton("Hapus",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                delete(karyawan.id);
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return (karyawans != null) ? karyawans.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNama, txtEmail, txtTgllahir, txtTelpon, txtJk;
        ImageView imgFoto;
        CardView cardView;
        Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNama = (TextView) itemView.findViewById(R.id.txt_nama);
            txtEmail = (TextView) itemView.findViewById(R.id.txt_email);
            txtTgllahir = (TextView) itemView.findViewById(R.id.txt_tgllahir);
            txtTelpon = (TextView) itemView.findViewById(R.id.txt_telpon);
            txtJk = (TextView) itemView.findViewById(R.id.txt_jk);
            imgFoto = (ImageView) itemView.findViewById(R.id.img_foto);
            cardView = (CardView) itemView.findViewById(R.id.card);
            btnDelete = (Button) itemView.findViewById(R.id.btn_delete);
        }
    }

    private void delete(String id){
        Call<String> call = api.deleteKaryawan(id);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String>
                    response) {
                Toast.makeText(context,"data berhasil dihapus",Toast.LENGTH_SHORT).show();
                notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(context,"tidak dapat menghapus data " + t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}
