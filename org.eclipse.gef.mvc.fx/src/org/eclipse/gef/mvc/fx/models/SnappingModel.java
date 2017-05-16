/*******************************************************************************
 * Copyright (c) 2017 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.gef.mvc.fx.models;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.common.beans.property.ReadOnlyListWrapperEx;
import org.eclipse.gef.common.collections.CollectionUtils;
import org.eclipse.gef.mvc.fx.parts.IContentPart;

import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;

/**
 * The {@link SnappingModel} stores {@link SnappingLocation}s for which feedback
 * is generated by the SnappingBehavior.
 */
public class SnappingModel {

	/**
	 * A {@link SnappingLocation} combines an {@link IContentPart}, a position
	 * coordinate in the scene coordinate system, and an {@link Orientation}.
	 * The position coordinate is evaluated in dependence of the location's
	 * {@link Orientation}. For horizontal locations, the position coordinate is
	 * a y-coordinate. For vertical locations, the position coordinate is an
	 * x-coordinate.
	 */
	public static class SnappingLocation {
		private IContentPart<? extends Node> part;
		private Orientation orientation;
		private double positionInScene;

		/**
		 * Constructs a new {@link SnappingLocation}.
		 *
		 * @param part
		 *            The {@link IContentPart} from which this location is
		 *            derived.
		 * @param orientation
		 *            The {@link Orientation} for this {@link SnappingLocation}.
		 * @param positionInScene
		 *            The position coordinate for this {@link SnappingLocation}.
		 *            The coordinate is either the x- or y-coordinate, depending
		 *            on the {@link Orientation}. For horizontal locations, the
		 *            y-coordinate needs to be specified. For vertical
		 *            locations, the x-coordinate needs to be specified.
		 */
		public SnappingLocation(IContentPart<? extends Node> part,
				Orientation orientation, double positionInScene) {
			this.part = part;
			this.orientation = orientation;
			this.positionInScene = positionInScene;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			SnappingLocation other = (SnappingLocation) obj;
			if (orientation != other.orientation) {
				return false;
			}
			if (part == null) {
				if (other.part != null) {
					return false;
				}
			} else if (!part.equals(other.part)) {
				return false;
			}
			if (Double.doubleToLongBits(positionInScene) != Double
					.doubleToLongBits(other.positionInScene)) {
				return false;
			}
			return true;
		}

		/**
		 * Returns a copy of this {@link SnappingLocation}.
		 *
		 * @return A copy of this {@link SnappingLocation}.
		 */
		public SnappingLocation getCopy() {
			return new SnappingLocation(getPart(), getOrientation(),
					getPositionInScene());
		}

		/**
		 * Returns the {@link Orientation} of this {@link SnappingLocation}.
		 *
		 * @return The {@link Orientation} of this {@link SnappingLocation}.
		 */
		public Orientation getOrientation() {
			return orientation;
		}

		/**
		 * Returns the {@link IContentPart} from which this
		 * {@link SnappingLocation} was derived.
		 *
		 * @return The {@link IContentPart} from which this
		 *         {@link SnappingLocation} was derived.
		 */
		public IContentPart<? extends Node> getPart() {
			return part;
		}

		/**
		 * Returns the position coordinate of this {@link SnappingLocation}.
		 *
		 * @return The position coordinate of this {@link SnappingLocation}.
		 */
		public double getPositionInScene() {
			return positionInScene;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((orientation == null) ? 0 : orientation.hashCode());
			result = prime * result + ((part == null) ? 0 : part.hashCode());
			long temp;
			temp = Double.doubleToLongBits(positionInScene);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		/**
		 * Sets the {@link Orientation} of this {@link SnappingLocation} to the
		 * given value.
		 *
		 * @param orientation
		 *            The new {@link Orientation} for this
		 *            {@link SnappingLocation}.
		 */
		public void setOrientation(Orientation orientation) {
			this.orientation = orientation;
		}

		/**
		 * Sets the {@link IContentPart} of this {@link SnappingLocation} to the
		 * given value.
		 *
		 * @param part
		 *            The new {@link IContentPart} for this
		 *            {@link SnappingLocation}.
		 */
		public void setPart(IContentPart<? extends Node> part) {
			this.part = part;
		}

		/**
		 * Sets the position coordinate of this {@link SnappingLocation} to the
		 * given value.
		 *
		 * @param positionInScene
		 *            The new position coordinate for this
		 *            {@link SnappingLocation}.
		 */
		public void setPositionInScene(double positionInScene) {
			this.positionInScene = positionInScene;
		}

		@Override
		public String toString() {
			return "SnappingLocation[part="
					+ getPart().getClass().getSimpleName() + "@"
					+ System.identityHashCode(getPart()) + ", orientation="
					+ getOrientation() + ", position=" + getPositionInScene()
					+ "]";
		}

	}

	/**
	 * Name of the {@link #snappingLocationsProperty()}.
	 */
	public static final String SNAPPING_LOCATIONS_PROPERTY = "snappingLocations";

	private ObservableList<SnappingLocation> snappingLocations = CollectionUtils
			.observableArrayList();

	private ReadOnlyListWrapper<SnappingLocation> snappingLocationsProperty = new ReadOnlyListWrapperEx<>(
			this, SNAPPING_LOCATIONS_PROPERTY, snappingLocations);

	/**
	 * Returns a {@link List} containing the {@link SnappingLocation}s currently
	 * stored in this {@link SnappingModel}.
	 *
	 * @return A {@link List} containing the {@link SnappingLocation}s currently
	 *         stored in this {@link SnappingModel}.
	 */
	public List<SnappingLocation> getSnappingLocations() {
		return snappingLocationsProperty.get();
	}

	/**
	 * Returns a {@link List} containing the {@link SnappingLocation}s that were
	 * derived from the given {@link IContentPart}.
	 *
	 * @param part
	 *            The {@link IContentPart} for which to return the derived
	 *            {@link SnappingLocation}s.
	 * @return A {@link List} containing the {@link SnappingLocation}s that were
	 *         derived from the given {@link IContentPart}.
	 */
	public List<SnappingLocation> getSnappingLocationsFor(
			IContentPart<? extends Node> part) {
		List<SnappingLocation> locations = new ArrayList<>();
		for (SnappingLocation p : getSnappingLocations()) {
			if (p.getPart() == part) {
				locations.add(p);
			}
		}
		return locations;
	}

	/**
	 * Replaces the {@link SnappingLocation}s that are stored in this
	 * {@link SnappingModel} by the given {@link List} of
	 * {@link SnappingLocation}s.
	 *
	 * @param snappingLocations
	 *            A {@link List} containing the new {@link SnappingLocation}s to
	 *            store in this {@link SnappingModel}.
	 */
	public void setSnappingLocations(
			List<? extends SnappingLocation> snappingLocations) {
		if (!snappingLocationsProperty.equals(snappingLocations)) {
			snappingLocationsProperty.setAll(snappingLocations);
		}
	}

	/**
	 * A read-only property containing the current {@link SnappingLocation}s.
	 *
	 * @return A read-only list property named
	 *         {@link #SNAPPING_LOCATIONS_PROPERTY}.
	 */
	public ReadOnlyListProperty<SnappingLocation> snappingLocationsProperty() {
		return snappingLocationsProperty.getReadOnlyProperty();
	}
}
