package ua.asparian.practice3

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator
import kotlin.math.exp
import kotlin.math.sqrt
import kotlin.math.PI
import kotlin.math.pow

class ProfitCalculatorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profit_calculator)

        val inputAveragePower: EditText = findViewById(R.id.input_average_power) // Середньодобова потужність Pc
        val inputOldDeviation: EditText = findViewById(R.id.input_old_deviation) // Старе σ
        val inputNewDeviation: EditText = findViewById(R.id.input_new_deviation) // Нове σ
        val inputCost: EditText = findViewById(R.id.input_cost) // Вартість електроенергії B
        val calculateButton: Button = findViewById(R.id.calculate_button)
        val resultText: TextView = findViewById(R.id.result_text)

        calculateButton.setOnClickListener {
            hideKeyboard()

            val Pc = inputAveragePower.text.toString().replace(",", ".").toDoubleOrNull() ?: 0.0
            val oldSigma = inputOldDeviation.text.toString().replace(",", ".").toDoubleOrNull() ?: 0.0
            val newSigma = inputNewDeviation.text.toString().replace(",", ".").toDoubleOrNull() ?: 0.0
            val B = inputCost.text.toString().replace(",", ".").toDoubleOrNull() ?: 0.0

            if (Pc > 0 && oldSigma > 0 && newSigma > 0 && B > 0) {
                // Обчислюємо прибуток до вдосконалення системи прогнозування
                val oldProfit = calculateProfit(Pc, oldSigma, B)

                // Обчислюємо прибуток після вдосконалення системи прогнозування
                val newProfit = calculateProfit(Pc, newSigma, B)

                // Різниця у прибутку
                val profitIncrease = newProfit - oldProfit

                resultText.text = "Прибуток до вдосконалення: ${"%.2f".format(oldProfit)} тис. грн\n" +
                        "Прибуток після вдосконалення: ${"%.2f".format(newProfit)} тис. грн\n" +
                        "Збільшення прибутку: ${"%.2f".format(profitIncrease)} тис. грн"
            } else {
                resultText.text = "Будь ласка, введіть коректні значення"
            }
        }
    }

    private fun calculateProfit(Pc: Double, sigma: Double, B: Double): Double {
        val deltaP = 0.05 * Pc // Допустиме відхилення
        val lowerBound = Pc - deltaP
        val upperBound = Pc + deltaP

        // Обчислюємо частку енергії, що генерується без небалансів
        val energyWithoutImbalance = integrateGaussian(Pc, sigma, lowerBound, upperBound)

        println("Pc: $Pc, sigma: $sigma, B: $B")
        println("Delta P: $deltaP, Lower Bound: $lowerBound, Upper Bound: $upperBound")
        println("Energy without imbalance: $energyWithoutImbalance")

        val W1 = Pc * 24 * energyWithoutImbalance
        val P1 = W1 * B // Прибуток від енергії без небалансів

        val W2 = Pc * 24 * (1 - energyWithoutImbalance)
        val penalty = W2 * B // Штраф за енергію з небалансами

        println("W1 (energy without imbalance): $W1, P1 (profit without imbalance): $P1")
        println("W2 (energy with imbalance): $W2, Penalty: $penalty")
        println("Result (Profit - Penalty): ${P1 - penalty}")

        return P1 - penalty
    }

    private fun integrateGaussian(Pc: Double, sigma: Double, lowerBound: Double, upperBound: Double): Double {
        val integrator = SimpsonIntegrator()
        val gaussianFunction = { p: Double ->
            (1 / (sigma * sqrt(2 * PI))) * exp(-((p - Pc).pow(2)) / (2 * sigma.pow(2)))
        }
        return integrator.integrate(1000, gaussianFunction, lowerBound, upperBound)
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
