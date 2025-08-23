package com.example.dummy.API


import com.example.dummy.Utils.SECRET_KEY
import com.example.dummy.models_stripe.CustomerModel
import com.example.dummy.models_stripe.EphemeralModel
import com.example.dummy.models_stripe.PaymentIntentModel
import retrofit2.Response
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query


interface ApiInterface {

    @Headers("Authorization: Bearer $SECRET_KEY")
    @POST("v1/customers")
    suspend fun getCustomer(): Response<CustomerModel>

    @Headers("Authorization: Bearer $SECRET_KEY","Stripe-Version: 2025-07-30.basil")
    @POST("v1/ephemeral_keys")
    suspend fun getEphemeralkey(
        @Query("customer") customer:String
    ) : Response<EphemeralModel>

    @Headers("Authorization: Bearer $SECRET_KEY")
    @POST("v1/payment_intents")
    suspend fun getPaymentIntent(
        @Query("customer") customer:String,
        @Query("amount") amount: Int ,
        @Query("currency") currency:String= "pkr",
        @Query("automatic_payment_methods[enabled]") automatePay:Boolean=true,
    ) : Response<PaymentIntentModel>


}