package com.example.sw0b_001.Homepage

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sw0b_001.Database.Datastore
import com.example.sw0b_001.EmailViewActivity
import com.example.sw0b_001.Modals.AvailablePlatformsModalFragment
import com.example.sw0b_001.Models.Messages.MessagesRecyclerAdapter
import com.example.sw0b_001.Models.Messages.MessagesViewModel
import com.example.sw0b_001.Models.Platforms.Platforms
import com.example.sw0b_001.R
import com.example.sw0b_001.TextViewActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomepageLoggedIn : Fragment(R.layout.fragment_homepage_logged_in) {

    private lateinit var messagesRecyclerView : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        messagesRecyclerView = view.findViewById(R.id.recents_recycler_view)

        configureRecyclerHandlers(view)

        view.findViewById<View>(R.id.homepage_compose_new_btn)
            .setOnClickListener { v ->
                showPlatformsModal(AvailablePlatformsModalFragment.Type.SAVED)
            }

        view.findViewById<View>(R.id.homepage_add_new_btn)
            .setOnClickListener { v ->
                showPlatformsModal(AvailablePlatformsModalFragment.Type.AVAILABLE)
            }

    }

    private fun configureRecyclerHandlers(view: View) {
        val linearLayoutManager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.VERTICAL, false);
        val noRecentMessagesText = view.findViewById<TextView>(R.id.no_recent_messages)
        messagesRecyclerView.layoutManager = linearLayoutManager

        val viewModel: MessagesViewModel by viewModels()

        CoroutineScope(Dispatchers.Default).launch {
            val availablePlatforms = Datastore.getDatastore(requireContext()).availablePlatformsDao()
                .fetchAllList();
            val recentRecyclerAdapter = MessagesRecyclerAdapter(availablePlatforms)

            activity?.runOnUiThread {
                messagesRecyclerView.adapter = recentRecyclerAdapter
                viewModel.getMessages(requireContext()).observe(viewLifecycleOwner) {
                    recentRecyclerAdapter.mDiffer.submitList(it) {
                        messagesRecyclerView.smoothScrollToPosition(0)
                    }
                    if (it.isNullOrEmpty()) {
                        activity?.runOnUiThread {
                            noRecentMessagesText.visibility = View.VISIBLE
                            view.findViewById<View>(R.id.homepage_no_message_image).visibility = View.VISIBLE
                            view.findViewById<View>(R.id.homepage_compose_new_btn1).visibility = View.GONE
                            view.findViewById<View>(R.id.homepage_add_new_btn1).visibility = View.GONE
                        }
                    }
                    else {
                        activity?.runOnUiThread {
                            noRecentMessagesText.visibility = View.GONE
                            view.findViewById<View>(R.id.homepage_no_message_image).visibility = View.GONE
                            view.findViewById<View>(R.id.homepage_compose_new_btn1).visibility = View.VISIBLE
                            view.findViewById<View>(R.id.homepage_add_new_btn1).visibility = View.VISIBLE

                            view.findViewById<View>(R.id.homepage_compose_new_btn1)
                                .setOnClickListener { v ->
                                    showPlatformsModal(AvailablePlatformsModalFragment.Type.SAVED)
                                }

                            view.findViewById<View>(R.id.homepage_add_new_btn1)
                                .setOnClickListener { v ->
                                    showPlatformsModal(AvailablePlatformsModalFragment.Type.AVAILABLE)
                                }
                        }
                    }
                }

                recentRecyclerAdapter.messageOnClickListener.observe(viewLifecycleOwner, Observer {
                    if(it != null) {
                        recentRecyclerAdapter.messageOnClickListener.value = null
                        when(it.first.type) {
                            Platforms.TYPE_TEXT -> {
                                startActivity(Intent(requireContext(), TextViewActivity::class.java).apply {
                                    val platform = Datastore.getDatastore(requireContext()).storedPlatformsDao()
                                        .fetch(it.second.toInt());
                                    putExtra("id", it.second)
                                    putExtra("platform_name", platform.name!!)
                                    putExtra("message_id", it.first.id)
                                })
                            }
                            Platforms.TYPE_EMAIL -> {
                                startActivity(Intent(requireContext(), EmailViewActivity::class.java).apply {
                                    val platform = Datastore.getDatastore(requireContext()).storedPlatformsDao()
                                        .fetch(it.second.toInt());
                                    putExtra("id", it.second)
                                    putExtra("platform_name", platform.name!!)
                                    putExtra("message_id", it.first.id)
                                })
                            }
                        }
                    }
                })
            }
        }
    }

    private fun showPlatformsModal(type: AvailablePlatformsModalFragment.Type) {
        val fragmentTransaction = activity?.supportFragmentManager?.beginTransaction()
        val platformsModalFragment = AvailablePlatformsModalFragment(type)
        fragmentTransaction?.add(platformsModalFragment, "store_platforms_tag")
        fragmentTransaction?.show(platformsModalFragment)
        activity?.runOnUiThread { fragmentTransaction?.commit() }
    }

}