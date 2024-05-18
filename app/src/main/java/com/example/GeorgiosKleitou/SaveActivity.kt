package com.example.GeorgiosKleitou

import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.widget.Button
import android.view.MenuItem
import android.view.View
import android.widget.Toast


class SaveActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.addpoi)

        val createbutton = findViewById<Button>(R.id.btn1)
        createbutton.setOnClickListener(this)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onClick(v: View?) {
        val name = findViewById<EditText>(R.id.et1)
        val type = findViewById<EditText>(R.id.et2)
        val description = findViewById<EditText>(R.id.et3)

        var Name = ""
        var Type = ""
        var Description = ""

        when (v?.id) {
            R.id.btn1 -> {
                Name = name.text.toString()
                Type = type.text.toString()
                Description = description.text.toString()
                returnInfo (Name, Type, Description)
                Toast.makeText(this, "POI Added", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun returnInfo (name: String, type: String, description: String ) {
        val intent = Intent()
        val bundle = bundleOf("com.example.name" to name, "com.example.type" to type,
            "com.example.description" to description)
        intent.putExtras(bundle)
        setResult(RESULT_OK, intent)
        finish()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.addPOI -> {
                Toast.makeText(this, "Add POI Chosen", Toast.LENGTH_SHORT).show()
                val intent = Intent(this,SaveActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.MainActivity -> {
                Toast.makeText(this, "Main Activity Chosen", Toast.LENGTH_SHORT).show()
                val intent = Intent(this,MainActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return false
    }
}