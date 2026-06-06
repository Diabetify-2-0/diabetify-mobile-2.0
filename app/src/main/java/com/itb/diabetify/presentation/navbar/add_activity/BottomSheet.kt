package com.itb.diabetify.presentation.navbar.add_activity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.itb.diabetify.R
import com.itb.diabetify.presentation.common.PrimaryButton
import com.itb.diabetify.ui.theme.poppinsFontFamily

@Composable
fun BottomSheet(
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
    questionType: String = "weight",
    currentNumericValue: String? = null,
    currentSelectionValue: String? = null,
    viewModel: AddActivityViewModel
) {
    // States
    val surveyQuestions = viewModel.surveyQuestions
    val currentQuestion = surveyQuestions.find { it.id == questionType } ?: surveyQuestions.first()

    if (isVisible) {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .clickable { onDismissRequest() },
                contentAlignment = Alignment.BottomCenter
            ) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(300)) +
                            slideInVertically(
                                animationSpec = tween(300, easing = FastOutSlowInEasing),
                                initialOffsetY = { it / 2 }
                            )
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(),
                        color = Color.Transparent
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = colorResource(id = R.color.white)
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 8.dp
                            ),
                            onClick = {}
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            ) {
                                // Header
                                Text(
                                    text = currentQuestion.category,
                                    fontFamily = poppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = colorResource(id = R.color.primary)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Question
                                Text(
                                    text = currentQuestion.questionText,
                                    fontFamily = poppinsFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )

                                // Content based on question type
                                when (currentQuestion.questionType) {
                                    DataInputQuestionType.Numeric -> NumericInput(
                                        question = currentQuestion,
                                        currentValue = currentNumericValue,
                                        onSave = {
                                            onDismissRequest()
                                        },
                                        viewModel = viewModel
                                    )
                                    DataInputQuestionType.Selection -> SelectionInput(
                                        question = currentQuestion,
                                        currentValue = currentSelectionValue,
                                        onSave = {
                                            onDismissRequest()
                                        },
                                        viewModel = viewModel
                                    )
                                    DataInputQuestionType.Pregnancy -> PregnancyInput(
                                        question = currentQuestion,
                                        currentValue = currentSelectionValue,
                                        onSave = {
                                            onDismissRequest()
                                        },
                                        viewModel = viewModel
                                    )
                                    DataInputQuestionType.Hypertension -> HypertensionInput(
                                        viewModel = viewModel,
                                        onSave = { 
                                            onDismissRequest()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NumericInput(
    question: DataInputQuestion,
    currentValue: String? = null,
    onSave: (String) -> Unit,
    viewModel: AddActivityViewModel
) {
    val isLoading = viewModel.addActivityState.value.isLoading || viewModel.updateActivityState.value.isLoading || viewModel.updateProfileState.value.isLoading || viewModel.predictionState.value.isLoading
    var inputValue by remember { mutableStateOf(currentValue ?: "") }
    var hasSubmitted by remember { mutableStateOf(false) }
    
    val fieldState = when (question.id) {
        "weight" -> viewModel.weightFieldState.value
        "height" -> viewModel.heightFieldState.value
        else -> null
    }
    
    val errorMessage = fieldState?.error
    val canSubmit = viewModel.isFieldValid(question.id)
    
    LaunchedEffect(isLoading, hasSubmitted) {
        if (hasSubmitted && !isLoading) {
            onSave(inputValue)
            hasSubmitted = false
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(18.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = inputValue,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.all { char -> char.isDigit() || char == '.' }) {
                        inputValue = newValue
                        when (question.id) {
                            "weight" -> viewModel.setWeightValue(newValue)
                            "height" -> viewModel.setHeightValue(newValue)
                        }
                    }
                },
                textStyle = TextStyle(
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = colorResource(R.color.black)
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    question.numericUnit?.let {
                        Text(
                            text = question.numericUnit,
                            modifier = Modifier.padding(end = 10.dp),
                            fontFamily = poppinsFontFamily,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                },
                isError = errorMessage != null
            )
            
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = colorResource(R.color.red),
                    fontSize = 12.sp,
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Normal,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Submit button
        PrimaryButton(
            text = "Simpan",
            onClick = { 
                hasSubmitted = true
                when (question.id) {
                    "weight", "height" -> {
                        val isValid = viewModel.validateProfileFields()
                        if (isValid) {
                            viewModel.updateProfile(question.id)
                        }
                    }
                }
            },
            enabled = canSubmit && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(50.dp),
            isLoading = isLoading
        )
    }
}

@Composable
fun SelectionInput(
    question: DataInputQuestion,
    currentValue: String? = null,
    onSave: (String) -> Unit,
    viewModel: AddActivityViewModel
) {
    val isLoading = viewModel.addActivityState.value.isLoading || viewModel.updateActivityState.value.isLoading || viewModel.updateProfileState.value.isLoading || viewModel.predictionState.value.isLoading
    val initialValue = when (currentValue?.lowercase()) {
        "true" -> "yes"
        "false" -> "no"
        else -> currentValue
    }
    
    var selectedOption by remember { mutableStateOf(initialValue) }
    var hasSubmitted by remember { mutableStateOf(false) }
    val fieldState = when (question.id) {
        "hypertension" -> viewModel.hypertensionFieldState.value
        "cholesterol" -> viewModel.cholesterolFieldState.value
        "bloodline" -> viewModel.bloodlineFieldState.value
        "activity" -> viewModel.workoutFieldState.value
        else -> null
    }
    
    val errorMessage = fieldState?.error
    val canSubmit = viewModel.isFieldValid(
        when (question.id) {
            "activity" -> "workout"
            else -> question.id
        }
    )
    
    LaunchedEffect(isLoading, hasSubmitted) {
        if (hasSubmitted && !isLoading) {
            selectedOption?.let { onSave(it) }
            hasSubmitted = false
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Radio options
            question.options.forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { 
                            selectedOption = option.id
                            when (question.id) {
                                "hypertension" -> viewModel.setHypertensionValue((option.id == "yes").toString())
                                "cholesterol" -> viewModel.setCholesterolValue((option.id == "yes").toString())
                                "bloodline" -> viewModel.setBloodlineValue((option.id == "yes").toString())
                                "activity" -> viewModel.setWorkoutValue(if (option.id == "yes") "true" else "false")
                            }
                        }
                ) {
                    RadioButton(
                        selected = selectedOption == option.id,
                        onClick = { 
                            selectedOption = option.id
                            when (question.id) {
                                "hypertension" -> viewModel.setHypertensionValue((option.id == "yes").toString())
                                "cholesterol" -> viewModel.setCholesterolValue((option.id == "yes").toString())
                                "bloodline" -> viewModel.setBloodlineValue((option.id == "yes").toString())
                                "activity" -> viewModel.setWorkoutValue(if (option.id == "yes") "true" else "false")
                            }
                        },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = if (errorMessage != null) colorResource(R.color.red) else colorResource(id = R.color.primary),
                            unselectedColor = if (errorMessage != null) colorResource(R.color.red) else colorResource(id = R.color.black)
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = option.label,
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }
            
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = colorResource(R.color.red),
                    fontSize = 12.sp,
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Normal,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Submit button
        PrimaryButton(
            text = "Simpan",
            onClick = { 
                hasSubmitted = true
                when (question.id) {
                    "hypertension", "cholesterol", "bloodline" -> {
                        val isValid = viewModel.validateProfileFields()
                        if (isValid) {
                            viewModel.updateProfile(question.id)
                        }
                    }
                    "activity" -> {
                        val isValid = viewModel.validateWorkoutField()
                        if (isValid) {
                            viewModel.handleWorkout()
                        }
                    }
                }
            },
            enabled = canSubmit && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(50.dp),
            isLoading = isLoading
        )
    }
}

@Composable
fun PregnancyInput(
    question: DataInputQuestion,
    currentValue: String? = null,
    onSave: (String) -> Unit,
    viewModel: AddActivityViewModel
) {
    val isLoading = viewModel.addActivityState.value.isLoading || viewModel.updateActivityState.value.isLoading || viewModel.updateProfileState.value.isLoading || viewModel.predictionState.value.isLoading
    var selectedOption by remember { mutableStateOf(currentValue) }
    var hasSubmitted by remember { mutableStateOf(false) }
    
    val fieldState = viewModel.birthFieldState.value
    val errorMessage = fieldState.error
    val canSubmit = viewModel.isFieldValid("birth")
    
    LaunchedEffect(isLoading, hasSubmitted) {
        if (hasSubmitted && !isLoading) {
            selectedOption?.let { onSave(it) }
            hasSubmitted = false
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Radio options
            question.options.forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { 
                            selectedOption = option.id
                            val birthValue = (option.id.toIntOrNull() ?: 0)
                            viewModel.setBirthValue(birthValue.toString())
                        }
                ) {
                    RadioButton(
                        selected = selectedOption == option.id,
                        onClick = { 
                            selectedOption = option.id
                            val birthValue = (option.id.toIntOrNull() ?: 0)
                            viewModel.setBirthValue(birthValue.toString())
                        },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = if (errorMessage != null) colorResource(R.color.red) else colorResource(id = R.color.primary),
                            unselectedColor = if (errorMessage != null) colorResource(R.color.red) else colorResource(id = R.color.black)
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = option.label,
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }
            
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = colorResource(R.color.red),
                    fontSize = 12.sp,
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Normal,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Submit button
        PrimaryButton(
            text = "Simpan",
            onClick = { 
                hasSubmitted = true
                val isValid = viewModel.validateProfileFields()
                if (isValid) {
                    viewModel.updateProfile("birth")
                }
            },
            enabled = canSubmit && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(50.dp),
            isLoading = isLoading
        )
    }
}

@Composable
fun HypertensionInput(
    viewModel: AddActivityViewModel,
    onSave: (String) -> Unit
) {
    val isLoading = viewModel.addActivityState.value.isLoading || viewModel.updateActivityState.value.isLoading || viewModel.updateProfileState.value.isLoading || viewModel.predictionState.value.isLoading
    var knowsBPValue by remember { mutableStateOf<String?>(null) }
    var hasSubmitted by remember { mutableStateOf(false) }
    
    LaunchedEffect(isLoading, hasSubmitted) {
        if (hasSubmitted && !isLoading) {
            onSave(viewModel.hypertensionFieldState.value.text)
            hasSubmitted = false
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { knowsBPValue = "yes" }
        ) {
            RadioButton(
                selected = knowsBPValue == "yes",
                onClick = { knowsBPValue = "yes" },
                colors = RadioButtonDefaults.colors(
                    selectedColor = colorResource(id = R.color.primary)
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Ya",
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color.Black
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { knowsBPValue = "no" }
        ) {
            RadioButton(
                selected = knowsBPValue == "no",
                onClick = { knowsBPValue = "no" },
                colors = RadioButtonDefaults.colors(
                    selectedColor = colorResource(id = R.color.primary)
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Tidak",
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color.Black
            )
        }

        when (knowsBPValue) {
            "yes" -> {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Masukkan nilai tekanan darah sistolik:",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    OutlinedTextField(
                        value = viewModel.systolicFieldState.value.text,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                viewModel.setSystolicValue(newValue)
                                if (newValue.isNotEmpty() && viewModel.diastolicFieldState.value.text.isNotEmpty()) {
                                    val systolic = newValue.toIntOrNull() ?: 0
                                    val diastolic = viewModel.diastolicFieldState.value.text.toIntOrNull() ?: 0
                                    val hasHypertension = systolic >= 140 || diastolic >= 90
                                    viewModel.setHypertensionValue(hasHypertension.toString())
                                }
                            }
                        },
                        textStyle = TextStyle(
                            fontFamily = poppinsFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = colorResource(R.color.black)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Text(
                                text = "mmHg",
                                modifier = Modifier.padding(end = 10.dp),
                                fontFamily = poppinsFontFamily,
                                fontWeight = FontWeight.Medium,
                            )
                        },
                        isError = viewModel.systolicFieldState.value.error != null
                    )
                    
                    viewModel.systolicFieldState.value.error?.let { error ->
                        Text(
                            text = error,
                            color = colorResource(R.color.red),
                            fontSize = 12.sp,
                            fontFamily = poppinsFontFamily,
                            fontWeight = FontWeight.Normal,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Masukkan nilai tekanan darah diastolik:",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    OutlinedTextField(
                        value = viewModel.diastolicFieldState.value.text,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                viewModel.setDiastolicValue(newValue)
                                if (newValue.isNotEmpty() && viewModel.systolicFieldState.value.text.isNotEmpty()) {
                                    val systolic = viewModel.systolicFieldState.value.text.toIntOrNull() ?: 0
                                    val diastolic = newValue.toIntOrNull() ?: 0
                                    val hasHypertension = systolic >= 140 || diastolic >= 90
                                    viewModel.setHypertensionValue(hasHypertension.toString())
                                }
                            }
                        },
                        textStyle = TextStyle(
                            fontFamily = poppinsFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = colorResource(R.color.black)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Text(
                                text = "mmHg",
                                modifier = Modifier.padding(end = 10.dp),
                                fontFamily = poppinsFontFamily,
                                fontWeight = FontWeight.Medium,
                            )
                        },
                        isError = viewModel.diastolicFieldState.value.error != null
                    )
                    
                    viewModel.diastolicFieldState.value.error?.let { error ->
                        Text(
                            text = error,
                            color = colorResource(R.color.red),
                            fontSize = 12.sp,
                            fontFamily = poppinsFontFamily,
                            fontWeight = FontWeight.Normal,
                        )
                    }
                }
            }
            "no" -> {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Apakah Anda didiagnosis memiliki tekanan darah tinggi (hipertensi)?",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable { viewModel.setHypertensionValue("true") }
                    ) {
                        RadioButton(
                            selected = viewModel.hypertensionFieldState.value.text == "true",
                            onClick = { viewModel.setHypertensionValue("true") },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = if (viewModel.hypertensionFieldState.value.error != null) colorResource(R.color.red) else colorResource(id = R.color.primary),
                                unselectedColor = if (viewModel.hypertensionFieldState.value.error != null) colorResource(R.color.red) else colorResource(id = R.color.black)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ya",
                            fontFamily = poppinsFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable { viewModel.setHypertensionValue("false") }
                    ) {
                        RadioButton(
                            selected = viewModel.hypertensionFieldState.value.text == "false",
                            onClick = { viewModel.setHypertensionValue("false") },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = if (viewModel.hypertensionFieldState.value.error != null) colorResource(R.color.red) else colorResource(id = R.color.primary),
                                unselectedColor = if (viewModel.hypertensionFieldState.value.error != null) colorResource(R.color.red) else colorResource(id = R.color.black)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tidak",
                            fontFamily = poppinsFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }
                    
                    viewModel.hypertensionFieldState.value.error?.let { error ->
                        Text(
                            text = error,
                            color = colorResource(R.color.red),
                            fontSize = 12.sp,
                            fontFamily = poppinsFontFamily,
                            fontWeight = FontWeight.Normal,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        PrimaryButton(
            text = "Simpan",
            onClick = { 
                hasSubmitted = true
                val isValid = viewModel.validateProfileFields()
                if (isValid) {
                    viewModel.updateProfile("hypertension")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(50.dp),
            enabled = when (knowsBPValue) {
                "yes" -> viewModel.canUpdateBloodPressure()
                "no" -> viewModel.isFieldValid("hypertension")
                else -> false
            } && !isLoading,
            isLoading = isLoading
        )
    }
}
