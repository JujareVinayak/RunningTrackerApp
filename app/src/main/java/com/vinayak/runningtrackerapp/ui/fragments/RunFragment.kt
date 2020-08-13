package com.vinayak.runningtrackerapp.ui.fragments

import android.Manifest
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.vinayak.runningtrackerapp.R
import com.vinayak.runningtrackerapp.adapters.RunAdapter
import com.vinayak.runningtrackerapp.enums.SortType
import com.vinayak.runningtrackerapp.ui.viewmodels.MainViewModel
import com.vinayak.runningtrackerapp.util.Constants
import com.vinayak.runningtrackerapp.util.Constants.REQUEST_CODE_FOR_LOCATION_PERMISSIONS
import com.vinayak.runningtrackerapp.util.TrackingUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_run.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject

@AndroidEntryPoint
class RunFragment:Fragment(R.layout.fragment_run), EasyPermissions.PermissionCallbacks {

    private val mainViewModel:MainViewModel by viewModels()
    private lateinit var runAdapter: RunAdapter
    @Inject
    lateinit var sharedPref: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestAppPermissions()
        var name = sharedPref.getString(Constants.KEY_NAME,"")
        val toolbarText = "Let's go, $name!"
        requireActivity().tvToolbarTitle.text = toolbarText
        setupRecyclerView()
        fab.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }
//        mainViewModel.runsSortedByDate.observe(viewLifecycleOwner, Observer {
//            runAdapter.submitList(it)
//        })
        when(mainViewModel.sortType) {
            SortType.DATE -> spFilter.setSelection(0)
            SortType.RUNNING_TIME -> spFilter.setSelection(1)
            SortType.DISTANCE -> spFilter.setSelection(2)
            SortType.AVG_SPEED -> spFilter.setSelection(3)
            SortType.CALORIES_BURNED -> spFilter.setSelection(4)
        }

        spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                when(pos) {
                    0 -> mainViewModel.sortRuns(SortType.DATE)
                    1 -> mainViewModel.sortRuns(SortType.RUNNING_TIME)
                    2 -> mainViewModel.sortRuns(SortType.DISTANCE)
                    3 -> mainViewModel.sortRuns(SortType.AVG_SPEED)
                    4 -> mainViewModel.sortRuns(SortType.CALORIES_BURNED)
                }
            }
        }

        mainViewModel.runs.observe(viewLifecycleOwner, Observer {
           runAdapter.submitList(it)
       })
    }

    private fun requestAppPermissions(){
        if(TrackingUtils.hasLocationPermissions(requireContext())){
            return
        }else{
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                EasyPermissions.requestPermissions(this,
                    "Accept permissions to use app.",REQUEST_CODE_FOR_LOCATION_PERMISSIONS,
                Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION)
            }else{
                EasyPermissions.requestPermissions(this,
                    "Accept permissions to use app.",REQUEST_CODE_FOR_LOCATION_PERMISSIONS,
                    Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this,perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }else{
            requestAppPermissions()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }

    private fun setupRecyclerView() = rvRuns.apply {
        runAdapter = RunAdapter()
        adapter = runAdapter
        layoutManager = LinearLayoutManager(requireContext())
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                CoroutineScope(Dispatchers.IO).launch {
                    val run = mainViewModel.runs.value!!.get(viewHolder.layoutPosition)
                    mainViewModel.mainRepository.deleteRun(run)
                    Snackbar.make(requireView(), "Deleted ${run.id}", Snackbar.LENGTH_SHORT).show()
                }

            }

        }).attachToRecyclerView(rvRuns)
    }

}