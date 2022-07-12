package com.project.helpmeat.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.project.helpmeat.R
import com.project.helpmeat.constant.Constants
import com.project.helpmeat.controller.GrillSettingsDataController
import com.project.helpmeat.controller.GrillSettingsDataObserver
import com.project.helpmeat.controller.GrillSettingsLayoutController
import com.project.helpmeat.controller.GrillSettingsLayoutController.Step
import com.project.helpmeat.utils.AnimationUtils
import com.project.helpmeat.utils.ResourceUtils
import com.project.helpmeat.view.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GrillSettingsFragment : BaseFragment(), GrillSettingsDataObserver {
    companion object {
        const val ALPHA_SHOW = 1.0F
        const val ALPHA_TRANSPARENT = 0.5F
        const val ALPHA_HIDE = 0.0F

        const val BLINK_ANIMATION_DURATION = 500L
    }

    private val mTextList = ArrayList<TextView>()

    private lateinit var mMeatImage: ImageView
    private lateinit var mMeatButton: TextView
    private lateinit var mWidthButton: TextView
    private lateinit var mGrillButton: TextView
    private lateinit var mDegreeButton: TextView

    private var mPreviousStep = Step.MEAT
    private var mCurrentStep = Step.MEAT

    @Inject
    lateinit var mGrillSettingsDataController: GrillSettingsDataController
    private lateinit var mGrillSettingsLayoutController: GrillSettingsLayoutController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_grill_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val root = view.findViewById<RelativeLayout>(R.id.fragment_grill_settings_content_container)
        limitContentViewArea(root, false)

        initGrillSettingsControllers(view)
        initSettingMainComponents(view)
        playCurrentStep()
    }

    override fun onDestroyView() {
        mGrillSettingsDataController.removeObserverAll()

        super.onDestroyView()
    }

    private fun initGrillSettingsControllers(view: View) {
        mGrillSettingsDataController.addObserver(this)
        mGrillSettingsLayoutController = GrillSettingsLayoutController(requireContext(), mGrillSettingsDataController, view)
    }

    private fun initSettingMainComponents(view: View) {
        mTextList.add(view.findViewById(R.id.fragment_grill_settings_text_first))
        mTextList.add(view.findViewById(R.id.fragment_grill_settings_text_second))
        mTextList.add(view.findViewById(R.id.fragment_grill_settings_text_third))

        mMeatImage = view.findViewById(R.id.fragment_grill_settings_meat_image)

        mMeatButton = view.findViewById(R.id.fragment_grill_settings_meat_button)
        mMeatButton.setOnTouchListener(mOnTouchListener)
        mMeatButton.setOnClickListener {
            mGrillSettingsLayoutController.showLayout(Step.MEAT)
        }

        mWidthButton = view.findViewById(R.id.fragment_grill_settings_width_button)
        mWidthButton.setOnTouchListener(mOnTouchListener)

        mGrillButton = view.findViewById(R.id.fragment_grill_settings_grill_button)
        mGrillButton.setOnTouchListener(mOnTouchListener)

        mDegreeButton = view.findViewById(R.id.fragment_grill_settings_state_button)
        mDegreeButton.setOnTouchListener(mOnTouchListener)
    }

    private fun playCurrentStep() {
        val textArray: Array<String>
        val button = when (mCurrentStep) {
            Step.MEAT -> {
                textArray = getMeatSettingDescription()
                mMeatButton
            }
            Step.WIDTH -> {
                textArray = getWidthSettingDescription()
                mWidthButton
            }
            Step.GRILL -> {
                textArray = getGrillSettingDescription()
                mGrillButton
            }
            Step.DEGREE -> {
                textArray = getDegreeSettingDescription()
                mDegreeButton
            }
            Step.FINISH -> {
                textArray = getFinishDescription()
                null
            }
        }
        for (i in textArray.indices) {
            if (textArray[i].isEmpty()) {
                mTextList[i].visibility = View.GONE
            } else {
                mTextList[i].visibility = View.VISIBLE
                mTextList[i].text = textArray[i]
            }
        }

        val context = requireContext()
        button?.let {
            AnimationUtils.playBlinkAnimation(BLINK_ANIMATION_DURATION, button)
            button.background = context.getDrawable(R.drawable.bg_rounded_rectangle_50_pink)
            button.setTextColor(context.getColor(R.color.white))
        }
    }

    private fun onStepCompleted() {
        mPreviousStep = mCurrentStep
        val button = when (mPreviousStep) {
            Step.MEAT -> {
                mCurrentStep = Step.WIDTH
                mMeatButton
            }
            Step.WIDTH -> {
                mCurrentStep = Step.GRILL
                mWidthButton
            }
            Step.GRILL -> {
                mCurrentStep = Step.DEGREE
                mGrillButton
            }
            Step.DEGREE -> {
                mCurrentStep = Step.FINISH
                mDegreeButton
            }
            Step.FINISH -> {
                null
            }
        }
        button?.clearAnimation()
        playCurrentStep()
    }

    override fun onMeatSelected(meatValue: Int) {
        when (Constants.getMeatType(meatValue)) {
            Constants.MeatType.MEAT_TYPE_FORK -> {
                mMeatImage.setImageDrawable(requireContext().getDrawable(R.drawable.fork_with_shadow))
            }
            Constants.MeatType.MEAT_TYPE_BEEF -> {
                mMeatImage.setImageDrawable(requireContext().getDrawable(R.drawable.beef_with_shadow))
            }
            Constants.MeatType.MEAT_TYPE_ERROR -> {}
        }

        with (mMeatButton) {
            text = ResourceUtils.getMeatName(requireContext(), meatValue)
            clearAnimation()
            background = context.getDrawable(R.drawable.bg_rounded_rectangle_50_pink)
            setTextColor(context.getColor(R.color.white))
        }
        onStepCompleted()
    }

    override fun onWidthSelected() {
        mWidthButton.text = ""
    }

    override fun onGrillSelected() {
        mGrillButton.text = ""
    }

    override fun onDegreeSelected() {
        mDegreeButton.text = ""
    }

    override fun needTouchAnimation() = true

    private fun getMeatSettingDescription(): Array<String> {
        val res = requireContext().resources

        val first = res.getString(R.string.meat_description_text_first)
        val second = res.getString(R.string.meat_description_text_second)
        val third = res.getString(R.string.meat_description_text_third)

        return arrayOf(first, second, third)
    }

    private fun getWidthSettingDescription(): Array<String> {
        val res = requireContext().resources

        val first = res.getString(R.string.width_description_text_first)
        val second = res.getString(R.string.width_description_text_second)
        val third = res.getString(R.string.width_description_text_third)

        return arrayOf(first, second, third)
    }

    private fun getGrillSettingDescription(): Array<String> {
        val res = requireContext().resources

        val second = res.getString(R.string.grill_description_text_second)
        val third = res.getString(R.string.grill_description_text_third)

        return arrayOf("", second, third)
    }

    private fun getDegreeSettingDescription(): Array<String> {
        val res = requireContext().resources

        val first = res.getString(R.string.degree_description_text_first)
        val second = res.getString(R.string.degree_description_text_second)
        val third = res.getString(R.string.degree_description_text_third)

        return arrayOf(first, second, third)
    }

    private fun getFinishDescription(): Array<String> {
        val res = requireContext().resources

        val first = res.getString(R.string.finish_description_text_first)
        val third = res.getString(R.string.finish_description_text_third)

        return arrayOf(first, "", third)
    }
}