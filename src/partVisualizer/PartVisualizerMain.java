package partVisualizer;

import java.io.IOException;

public class PartVisualizerMain {

	public static void main(String[] args) throws ClassNotFoundException, IOException, InterruptedException {
		PartVisualizer partVisualizer = new PartVisualizer();

		partVisualizer.initialization();

		partVisualizer.actionLoop();

		partVisualizer.close();

	}

}
