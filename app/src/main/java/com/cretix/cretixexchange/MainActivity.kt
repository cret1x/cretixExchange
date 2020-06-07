package com.cretix.cretixexchange

import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.URL


class MainActivity : AppCompatActivity() {
    companion object{
        val exchange_rates = arrayOf("RUB", "USD", "EUR", "GBP", "JPY", "BGN")
        var select1: Spinner? = null
        var select2: Spinner? = null
        var input1: TextView? = null
        var input2: TextView? = null
        var convert_from = "RUB"
        var convert_to = "RUB"
        var rates: SharedPreferences? = null
    }

    fun updateFieldTo(){
        if (input1!!.text.isNotEmpty()){
            val multiplier = rates!!.getFloat("$convert_from-$convert_to", 1F)
            input2!!.text = (input1!!.text.toString().toFloat() * multiplier).toString()
        }
        else
            input2!!.text = ""
    }

    fun updateFieldFrom(){
        if (input2!!.text.isNotEmpty()){
            val multiplier = rates!!.getFloat("$convert_to-$convert_from", 1F)
            input1!!.text = (input2!!.text.toString().toFloat() * multiplier).toString()
        }
        else
            input1!!.text = ""
    }

    fun updateRate(){
        val asyncRequest =  AsyncRequest()
        asyncRequest.execute("https://api.exchangeratesapi.io/latest?base=")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rates = getSharedPreferences("rates",0)
        select1 = findViewById(R.id.select1)
        select2 = findViewById(R.id.select2)
        input1 = findViewById(R.id.from_input)
        input2 = findViewById(R.id.to_input)
        updateRate()



        input1!!.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateFieldTo()
            }
        })

        val rateAdapterFrom: ArrayAdapter<String> = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, exchange_rates)
        rateAdapterFrom.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        select1?.adapter = rateAdapterFrom
        select1?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                convert_from = select1!!.selectedItem.toString()
                updateFieldTo()
            }
        }
        select2?.adapter = rateAdapterFrom
        select2?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                convert_to = select2!!.selectedItem.toString()
                updateFieldTo()
            }
        }
    }

    internal class AsyncRequest : AsyncTask<String?, Int?, String>() {
        override fun doInBackground(vararg params: String?): String {
            for (rate in exchange_rates){
                val response = JSONObject(JSONObject(URL(params[0] + rate).readText())["rates"].toString())
                for (entry in response.keys()){
                    rates!!.edit().putFloat("$rate-$entry", response[entry].toString().toFloat()).apply()
                }
            }


            return "OK"
        }


    }
}
