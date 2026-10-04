<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#121212"
    android:padding="16dp">

    <TextView
        android:id="@+id/titleText"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:text="Tavin"
        android:textColor="#FFFFFF"
        android:textSize="32sp"
        android:textStyle="bold"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <RadioGroup
        android:id="@+id/modeGroup"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_marginTop="16dp"
        app:layout_constraintTop_toBottomOf="@id/titleText"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <RadioButton
            android:id="@+id/btnSender"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Sender"
            android:textColor="#FFFFFF" />

        <RadioButton
            android:id="@+id/btnReceiver"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Receiver"
            android:textColor="#FFFFFF" />
    </RadioGroup>

    <EditText
        android:id="@+id/serverInput"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:hint="IP do servidor: 192.168.0.10:8080"
        android:textColor="#FFFFFF"
        android:textColorHint="#B0B0B0"
        android:backgroundTint="#6200EE"
        android:layout_marginTop="16dp"
        app:layout_constraintTop_toBottomOf="@id/modeGroup"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <Button
        android:id="@+id/connectButton"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:text="Conectar"
        android:layout_marginTop="16dp"
        app:layout_constraintTop_toBottomOf="@id/serverInput"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <Button
        android:id="@+id/startButton"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:text="Iniciar"
        android:layout_marginTop="8dp"
        app:layout_constraintTop_toBottomOf="@id/connectButton"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <TextView
        android:id="@+id/statusText"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:text="Status: pronto"
        android:textColor="#FFFFFF"
        android:textSize="16sp"
        android:layout_marginTop="12dp"
        app:layout_constraintTop_toBottomOf="@id/startButton"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <ImageView
        android:id="@+id/previewImage"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:background="#1E1E1E"
        android:scaleType="fitCenter"
        android:layout_marginTop="16dp"
        app:layout_constraintTop_toBottomOf="@id/statusText"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintBottom_toBottomOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>

