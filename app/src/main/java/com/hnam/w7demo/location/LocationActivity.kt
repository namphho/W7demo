/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hnam.w7demo.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.hnam.w7demo.R
import kotlinx.android.synthetic.main.activity_location.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class LocationActivity : AppCompatActivity(), OnTaskCompleted {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        button_location.setOnClickListener {
            checkLocationRequest()
        }
    }

    companion object {
        private val TAG = LocationActivity::class.java.simpleName
        private const val REQUEST_LOCATION_PERMISSION: Int = 100
    }


    private fun checkLocationRequest() {
        if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION);
        } else {
            Log.d(TAG, "getLocation: permissions granted");
            getLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                // If the permission is granted, get the location,
                // otherwise, show a Toast
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation()
                } else {
                    Toast.makeText(this,R.string.location_permission_denied, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getLocation() {
        val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                textview_location.text = getString(R.string.location_text,
                        location.latitude,
                        location.longitude,
                        location.time)
                // Start the reverse geocode AsyncTask

                // Start the reverse geocode AsyncTask
                FetchAddressTask(this@LocationActivity,
                        this@LocationActivity).execute(location)
                textview_location.text = getString(R.string.address_text,
                        getString(R.string.loading),
                        System.currentTimeMillis());
            } else {
                textview_location.text = getString(R.string.no_location)
            }
        }
    }

    class FetchAddressTask(applicationContext: Context, val taskCompleted: OnTaskCompleted) : AsyncTask<Location, Void, String>() {
        private val TAG = FetchAddressTask::class.java.simpleName
        private val mContext: Context = applicationContext
        override fun doInBackground(vararg locations: Location): String {
            val geocoder = Geocoder(mContext, Locale.getDefault())
            val location: Location = locations[0]
            var addresses: List<Address>? = null
            var resultMessage = ""
            try {
                addresses = geocoder.getFromLocation(
                        location.latitude,
                        location.longitude,
                        // In this sample, get just a single address
                        1);
            } catch (ioException: IOException) {
                // Catch network or other I/O problems
                resultMessage = mContext
                        .getString(R.string.service_not_available);
                Log.e(TAG, resultMessage, ioException);
            } catch (illegalArgumentException: IllegalArgumentException) {
                // Catch invalid latitude or longitude values
                resultMessage = mContext
                        .getString(R.string.invalid_lat_long_used);
                Log.e(TAG, resultMessage + ". " +
                        "Latitude = " + location.latitude +
                        ", Longitude = " +
                        location.longitude, illegalArgumentException);
            }
            if (addresses == null || addresses.isEmpty()) {
                if (resultMessage.isEmpty()) {
                    resultMessage = mContext
                            .getString(R.string.no_address_found);
                    Log.e(TAG, resultMessage);
                }
            } else {
                // If an address is found, read it into resultMessage
                var address = addresses[0];
                var addressParts = ArrayList<String>();

                // Fetch the address lines using getAddressLine,
                // join them, and send them to the thread
                for (i in 0..address.maxAddressLineIndex) {
                    addressParts.add(address.getAddressLine(i));
                }

                resultMessage = TextUtils.join("\n", addressParts);
            }
            return resultMessage
        }

        override fun onPostExecute(address: String) {
            super.onPostExecute(address)
            taskCompleted.onTaskCompleted(address)
        }
    }

    override fun onTaskCompleted(result: String?) {
        // Update the UI
        textview_location.text = getString(R.string.address_text,
                result, System.currentTimeMillis());
    }
}