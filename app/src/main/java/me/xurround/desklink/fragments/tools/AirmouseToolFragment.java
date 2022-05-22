package me.xurround.desklink.fragments.tools;

import static android.content.Context.SENSOR_SERVICE;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import me.xurround.desklink.R;
import me.xurround.desklink.viewmodels.DeskControlViewModel;

public class AirmouseToolFragment extends Fragment
{
    private SensorManager sensorManager;
    private SensorEventListener sensorEventListener;

    private static final int MEASURE_MULTIPLIER = 4;

    private int x = 0;
    private int y = 0;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_tool_airmouse, container, false);

        DeskControlViewModel viewModel = new ViewModelProvider(requireActivity()).get(DeskControlViewModel.class);

        sensorEventListener = new SensorEventListener()
        {
            @Override
            public void onSensorChanged(SensorEvent event)
            {
                if (Math.sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2]) < 0.0001)
                    return;
                x = Math.round(event.values[2] * MEASURE_MULTIPLIER);
                y = Math.round(event.values[0] * MEASURE_MULTIPLIER);
                if (x != 0 || y != 0)
                {
                    viewModel.sendData(new byte[]
                    {
                        (byte)0xF0,
                        (byte)(x >> 8),
                        (byte)(x & 0xFF),
                        (byte)(y >> 8),
                        (byte)(y & 0xFF)
                    });
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) { }
        };

        sensorManager = (SensorManager) requireContext().getSystemService(SENSOR_SERVICE);
        Sensor gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(sensorEventListener, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);

        viewModel.setHardwareKeyEventListener(k ->
        {
            if (k == KeyEvent.KEYCODE_VOLUME_UP)
            {
                viewModel.sendData(new byte[] { (byte)0xB2 });
                return true;
            }
            if (k == KeyEvent.KEYCODE_VOLUME_DOWN)
            {
                viewModel.sendData(new byte[] { (byte)0xB1 });
                return true;
            }
            return false;
        });

        return view;
    }

    @Override
    public void onDestroy()
    {
        sensorManager.unregisterListener(sensorEventListener);
        super.onDestroy();
    }
}
