import okhttp3.ResponseBody // Importe bien ceci !
import com.bernado.pretbancaire.models.Pret
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
/*
interface ApiService {

    @GET("prets")
    fun getTousLesPrets(): Call<List<Pret>>

    @Headers("Content-Type: application/json") // <-- AJOUTE CECI
    @POST("prets")
    fun ajouterPret(@Body nouveauPret: Pret): Call<ResponseBody>

    @Headers("Content-Type: application/json") // <-- AJOUTE CECI
    @PUT("prets/{num_compte}")
    fun modifierPret(@Path("num_compte") numCompte: String, @Body pret: Pret): Call<ResponseBody>

    @DELETE("prets/{num_compte}")
    fun supprimerPret(@Path("num_compte") numCompte: String): Call<ResponseBody>
}*/
interface ApiService {

    @GET("prets")
    fun getTousLesPrets(): Call<List<Pret>>

    // On change le retour en Call<ResponseBody> pour éviter le crash de parsing
    @POST("prets")
    fun ajouterPret(@Body nouveauPret: Pret): Call<ResponseBody>

    // Nouveau : Modifier (PUT http://ip:8080/prets/12345)
    @PUT("prets/{num_compte}")
    fun modifierPret(@Path("num_compte") numCompte: String, @Body pret: Pret): Call<ResponseBody>

    // Nouveau : Supprimer (DELETE http://ip:8080/prets/12345)
    @DELETE("prets/{num_compte}")
    fun supprimerPret(@Path("num_compte") numCompte: String): Call<ResponseBody>
}