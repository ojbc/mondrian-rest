package org.ojbc.mondrian;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MeasureGroupWrapper {
	
	private String name;
	private List<String> measureReferences;
	private List<String> dimensionReferences;
	
	public MeasureGroupWrapper() {
		measureReferences = new ArrayList<String>();
		dimensionReferences = new ArrayList<String>();
	}

	public String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	public List<String> getMeasureReferences() {
		return Collections.unmodifiableList(measureReferences);
	}

	void setMeasureReferences(List<String> measureReferences) {
		this.measureReferences = measureReferences;
	}

	public List<String> getDimensionReferences() {
		return Collections.unmodifiableList(dimensionReferences);
	}

	void setDimensionReferences(List<String> dimensionReferences) {
		this.dimensionReferences = dimensionReferences;
	}

}
