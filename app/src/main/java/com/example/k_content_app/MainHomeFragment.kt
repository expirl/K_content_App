package com.example.k_content_app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.k_content_app.benner.RecommendFragment
import com.example.k_content_app.benner.RecommendFragment02
import com.example.k_content_app.benner.RecommendFragment03
import com.example.k_content_app.benner.RecommendViewpagerAdapter
import com.example.k_content_app.card_benner.CardAdapter
import com.example.k_content_app.card_benner.card_benner.CardData
import com.example.k_content_app.databinding.FragmentMainHomeBinding

class MainHomeFragment : Fragment() {

    private var _binding: FragmentMainHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewPager: ViewPager2
    private var currentPage = 0
    private val sliderHandler = Handler(Looper.getMainLooper())
    private lateinit var sliderThread: Thread

    private lateinit var recyclerView: RecyclerView
    private lateinit var cardAdapter: CardAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 추천 배너 설정
        viewPager = binding.homeRecommendVp
        val recommendAdapter = RecommendViewpagerAdapter(requireActivity())

        recommendAdapter.addFragment(RecommendFragment())
        recommendAdapter.addFragment(RecommendFragment02())
        recommendAdapter.addFragment(RecommendFragment03())
        recommendAdapter.addFragment(RecommendFragment())
        recommendAdapter.addFragment(RecommendFragment02())
        recommendAdapter.addFragment(RecommendFragment03())

        viewPager.adapter = recommendAdapter
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        // 추천 배너 자동 슬라이딩 설정
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandler.removeCallbacks(sliderRunnable)
                sliderHandler.postDelayed(sliderRunnable, 3000)
            }
        })

        startAutoSlide()

        // 퀴즈 버튼
        binding.quizButton.setOnClickListener {
            val intent = Intent(requireContext(), GameDescriptionActivity::class.java)
            startActivity(intent)
        }

        // 이벤트 버튼
        binding.eventButton.setOnClickListener {
            val intent = Intent(requireContext(), ApplyActivity::class.java)
            startActivity(intent)
        }

        // btn2 (마이페이지 이동)
        binding.btn2.setOnClickListener {
            Log.d("MainHomeFragment", "btn2 클릭됨")
            findNavController().navigate(R.id.action_mainHomeFragment_to_userInfoFragment)
        }

        // btn1 (검색화면 이동)
        binding.btn1.setOnClickListener {
            Log.d("MainHomeFragment", "btn1 클릭됨")
            findNavController().navigate(R.id.action_mainHomeFragment_to_searchingFragment)
        }

        // 최신 배너를 위한 RecyclerView 설정
        recyclerView = binding.cardList
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager

        val cardDataList = listOf(
            CardData("Card 1", R.drawable.banner1),
            CardData("Card 2", R.drawable.banner2),
            CardData("Card 3", R.drawable.banner3),
            CardData("Card 4", R.drawable.banner1)
        )

        cardAdapter = CardAdapter(requireContext(), cardDataList)
        recyclerView.adapter = cardAdapter
    }

    private fun startAutoSlide() {
        sliderThread = Thread(PagerRunnable())
        sliderThread.start()
    }

    private inner class PagerRunnable : Runnable {
        override fun run() {
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(2000)
                    sliderHandler.post { setPage() }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                    Thread.currentThread().interrupt()
                }
            }
        }
    }

    private fun setPage() {
        if (currentPage == viewPager.adapter?.itemCount) {
            currentPage = 0
        }
        viewPager.setCurrentItem(currentPage++, true)
    }

    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
        if (::sliderThread.isInitialized && !sliderThread.isInterrupted) {
            sliderThread.interrupt()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::sliderThread.isInitialized && !sliderThread.isAlive) {
            startAutoSlide()
        }
    }

    private val sliderRunnable = Runnable { setPage() }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
