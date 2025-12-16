package io.github.madsonpaulo.springcodegenerator.core.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JavaFieldModelDto {
	private String name;
	private String type;
	private String comment;
	private boolean primaryKey;

	@Getter(AccessLevel.NONE)
	private final List<JavaAnnotationModelDto> annotations = new ArrayList<>();

	public void addAnnotation(JavaAnnotationModelDto annotation) {
		if (annotation != null) {
			this.annotations.add(annotation);
		}
	}

	public List<JavaAnnotationModelDto> getAnnotationsSnapshot() {
		return List.copyOf(annotations);
	}

	public boolean hasAnnotations() {
		return !annotations.isEmpty();
	}

}
