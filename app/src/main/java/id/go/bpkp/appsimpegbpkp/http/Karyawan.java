
package id.go.bpkp.appsimpegbpkp.http;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Karyawan implements Serializable
{

    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("nama")
    @Expose
    public String nama;
    @SerializedName("email")
    @Expose
    public String email;
    @SerializedName("telpon")
    @Expose
    public String telpon;
    @SerializedName("jeniskelamin")
    @Expose
    public String jeniskelamin;
    @SerializedName("iddivisi")
    @Expose
    public String iddivisi;
    @SerializedName("tgllahir")
    @Expose
    public String tgllahir;
    @SerializedName("foto")
    @Expose
    public String foto;
    @SerializedName("jabatan")
    @Expose
    public Object jabatan;
    @SerializedName("namadivisi")
    @Expose
    public String namadivisi;
    private final static long serialVersionUID = 2719161138432967043L;

}
