package com.carTracker.carTracker;

import com.carTracker.carTracker.ui.JavaFxApplication;
import com.carTracker.carTracker.ui.SingleInstance;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

@SpringBootApplication
public class CarTrackerApplication {


	public static void moveNegativesToFront(int[] numbers) {
		int count = 0;
		int lenght = numbers.length;
		int spacing = 0;
		for (int i = 0; i < lenght; i++) {
			if (numbers[i] < 0) {
					spacing = numbers[count];
				numbers[count] = numbers[i];
				count++;
				numbers[i] =  spacing;


			}
			System.out.println(Arrays.toString(numbers));
		}
		System.out.println(Arrays.toString(numbers));
	}







	public static void main(String[] args) {
		if (!SingleInstance.acquire()) {
			return;
		}
		Application.launch(JavaFxApplication.class, args);
	}

}
