package xyz.aprildown.timer.app.timer.edit.media

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.github.deweyreed.tools.helper.focusAndShowKeyboard
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import xyz.aprildown.timer.app.timer.edit.databinding.DialogSkipTargetBinding
import xyz.aprildown.timer.domain.entities.SkipInGroupAction
import xyz.aprildown.timer.app.base.R as RBase

internal class SkipInGroupDialog(private val context: Context) {
    fun showTargetDialog(oldTarget: SkipInGroupAction.Target, func: (SkipInGroupAction.Target) -> Unit) {
        val builder = MaterialAlertDialogBuilder(context)
            .setTitle(RBase.string.name_loop_loop_hint)
            .setPositiveButton(RBase.string.ok, null)
            .setNegativeButton(RBase.string.cancel, null)

        val binding = DialogSkipTargetBinding.inflate(LayoutInflater.from(context))

        when (oldTarget) {
            SkipInGroupAction.Target.First -> binding.radioFirst.isChecked = true
            SkipInGroupAction.Target.Last -> binding.radioLast.isChecked = true
            is SkipInGroupAction.Target.Loops -> {
                binding.radioLoops.isChecked = true
                binding.edit.setText(oldTarget.loopNumbers.joinToString())
            }
        }
        binding.radioFirst.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.radioLast.isChecked = false
                binding.radioLoops.isChecked = false
            }
        }
        binding.radioLast.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.radioFirst.isChecked = false
                binding.radioLoops.isChecked = false
            }
        }
        binding.radioLoops.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.radioFirst.isChecked = false
                binding.radioLast.isChecked = false
                binding.edit.focusAndShowKeyboard()
            }
        }
        binding.edit.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.radioLoops.isChecked = true
                binding.edit.selectAll()
            }
        }

        builder.setView(binding.root)

        val dialog = builder.create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val newTarget = when {
                binding.radioFirst.isChecked -> SkipInGroupAction.Target.First
                binding.radioLast.isChecked -> SkipInGroupAction.Target.Last
                binding.radioLoops.isChecked -> {
                    val indices = binding.edit.text?.toString()?.split(Regex("\\D+"))
                        ?.mapNotNull { it.toIntOrNull() }
                        ?.map { it - 1 }
                        ?.toSet()
                    if (indices.isNullOrEmpty()) {
                        SkipInGroupAction.Target.Last
                    } else {
                        SkipInGroupAction.Target.Loops(loopIndices = indices)
                    }
                }
                else -> SkipInGroupAction.Target.Last
            }
            dialog.dismiss()
            func.invoke(newTarget)
        }
    }
}
