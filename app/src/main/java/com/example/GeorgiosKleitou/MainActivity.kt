package com.example.GeorgiosKleitou

import android.Manifest
import android.app.AlertDialog
import android.os.Bundle
import android.location.LocationManager
import android.location.LocationListener
import android.location.Location
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.github.kittinunf.fuel.json.responseJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem


class MainActivity : AppCompatActivity(), LocationListener {

    var lat = 0.0
    var lon = 0.0

    var newPoi: OverlayItem? = null

    lateinit var mp: ItemizedIconOverlay<OverlayItem>

    var poiNameList = mutableListOf<String?>()
    var poiTypeList = mutableListOf<String?>()
    var poiDescriptionList = mutableListOf<String?>()
    var poiLatList = mutableListOf<Double>()
    var poiLonList = mutableListOf<Double>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        setContentView(R.layout.activity_main)
        val map1 = findViewById<MapView>(R.id.map1)
        map1.controller.setZoom(14.0)
        map1.controller.setCenter(GeoPoint(51.05, -0.72))



        mp = ItemizedIconOverlay(this, arrayListOf<OverlayItem>(), null)
        map1.overlays.add(mp)

        requestLocation()


    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.addPOI -> {
                Toast.makeText(this, "Add POI Chosen", Toast.LENGTH_SHORT).show()
                val intent = Intent(this,SaveActivity::class.java)
                poiLauncher.launch(intent)
                return true
            }
            R.id.MainActivity -> {
                Toast.makeText(this, "Main Activity Chosen", Toast.LENGTH_SHORT).show()
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            return true
            }

            R.id.savePreference -> {
                Toast.makeText(this, "Saving User Preference", Toast.LENGTH_SHORT).show()
                val intent = Intent(this,PreferenceActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.loadPOI -> {
                Toast.makeText(this, "POIs Load", Toast.LENGTH_SHORT).show()
                loadPOI()
                return true
            }
            R.id.loadWeb -> {
                Toast.makeText(this, "POIs Load From Web", Toast.LENGTH_SHORT).show()
                loadPOIWeb()
                return true
            }
            R.id.savePOI -> {
                savePOI()
                return true
            }
        }
        return false
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {

                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestLocation()
                } else {
                    AlertDialog.Builder(this)
                        .setPositiveButton("OK", null)
                        .setMessage("Please ensure the GPS permission is enabled.")
                        .show()
                }
            }


        }


    }

    fun requestLocation() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val mgr = getSystemService(Context.LOCATION_SERVICE) as LocationManager

            mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)

        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
        }

    }

    override fun onLocationChanged(pointofinterest: Location) {
        val map1 = findViewById<MapView>(R.id.map1)

        map1.controller.setZoom(14.0)
        map1.controller.setCenter(GeoPoint(pointofinterest.latitude, pointofinterest.longitude))

        lat = pointofinterest.latitude
        lon = pointofinterest.longitude

    }

    override fun onProviderDisabled(provider: String) {
        Toast.makeText (this, "Provider disabled", Toast.LENGTH_LONG).show()
    }

    override fun onProviderEnabled(provider: String) {
        Toast.makeText (this, "Provider enabled", Toast.LENGTH_LONG).show()
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

    }

    val poiLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                it.data?.apply {
                    val name = this.getStringExtra("com.example.name")
                    val type = this.getStringExtra("com.example.type")
                    val description = this.getStringExtra("com.example.description")

                    val poiString = "Name: $name \nType: $type\nDescription: $description"

                    poiNameList.add(name)
                    poiTypeList.add(type)
                    poiDescriptionList.add(description)
                    poiLatList.add(lat)
                    poiLonList.add(lon)

                    newPoi = OverlayItem("$name", poiString, GeoPoint(lat, lon))
                    mp.addItem(newPoi)

                    val preference = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                    val status = preference.getBoolean("checker", false) ?: false
                    if (status == true){
                        saveToWeb()
                    }

                }
            }
        }

    fun savePOI(){
        lifecycleScope.launch{
            val database = PoiDatabase.getDatabase(application)
            for(poi in 0 until poiNameList.size){
                var name = poiNameList[poi]
                var type = poiTypeList[poi]
                var description = poiDescriptionList[poi]
                var latitude = poiLatList[poi]
                var longitude = poiLonList[poi]

                var POI = Pois(0,name,type,description,latitude,longitude)

                var id = 0L

                withContext(Dispatchers.IO){
                    id = database.PoiDao().insert(POI)
                }


            }

        }

        Toast.makeText (this, "POI added", Toast.LENGTH_LONG).show()
    }

    fun loadPOI(){
        lifecycleScope.launch{
            withContext(Dispatchers.IO){
                val database = PoiDatabase.getDatabase(application)
                mp.removeAllItems()
                var pois = database.PoiDao().getAllPois()
                for (POI in 0 until pois.size){
                    var poi = pois[POI]
                    poi?.apply {
                        var latitude = poi.lat
                        var longitude = poi.lon
                        var name = poi.name
                        var description = poi.description
                        var newPOI = OverlayItem(name, description, GeoPoint(latitude, longitude))

                        mp.addItem(newPOI)
                    }
                }
            }
        }
        Toast.makeText (this, "POIs loaded", Toast.LENGTH_LONG).show()
    }

    fun saveToWeb(){
        Toast.makeText (this, "saved to web", Toast.LENGTH_LONG).show()
    }

    fun loadPOIWeb(){
        mp.removeAllItems()
        val url = "http://10.0.2.2:3000/poi/all"
        url.httpGet().responseJson{request, response, result ->
            when (result){
                is Result.Success -> {
                    val array = result.get().array()
                    for (POI in 0 until array.length()){
                        val curObj = array.getJSONObject(POI)
                        var newPOI = OverlayItem(curObj.getString("name"), curObj.getString("description"),
                        GeoPoint(curObj.getString("lat").toDouble(), curObj.getString("lon").toDouble())
                            )
                        mp.addItem(newPOI)
                    }
                }

            is Result.Failure -> {
             Toast.makeText (this, result.error.message, Toast.LENGTH_LONG).show()
            }
            }
        }
    }
}





