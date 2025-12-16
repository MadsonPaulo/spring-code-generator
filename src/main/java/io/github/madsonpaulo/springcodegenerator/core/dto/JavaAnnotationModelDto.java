package io.github.madsonpaulo.springcodegenerator.core.dto;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class JavaAnnotationModelDto {
	private final String name;
	private String singleValue;

	@Getter(AccessLevel.NONE)
	private final Map<String, String> attributes = new LinkedHashMap<>();

	public JavaAnnotationModelDto(String name) {
		super();
		this.name = name;
	}

	public void addAttribute(String key, String value) {
		if (!StringUtils.hasText(key) || value == null) {
			return;
		}
		attributes.put(key.trim(), value.trim());
	}

	public Map<String, String> getAttributesSnapshot() {
		return Map.copyOf(attributes);
	}

	public boolean hasAttributes() {
		return !attributes.isEmpty();
	}

	public boolean hasSingleValue() {
		return singleValue != null;
	}

}
