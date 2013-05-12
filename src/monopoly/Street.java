/**
 * 
 */
package monopoly;

import java.util.Collection;

/**
 * @author thiemann
 *
 */
public class Street extends AProperty {
	final int[] rent;
	final Group colorGroup;
	
	State state;
	
	

	////////
	
	public int[] getRent() {
		return rent.clone();		// do not return the plain array!
	}

	public int getMortgageValue() {
		return price / 2;
	}

	public int getHouseCost() {
		return this.colorGroup.getHouseCost();
	}

	public Group getColorGroup() {
		return colorGroup;
	}

	/**
	 * @return the mortgage
	 */
	public boolean isMortgaged() {
		return this.state == State.MORTGAGED;
	}

	/**
	 * @return the state
	 */
	public State getState() {
		return state;
	}

	private Street(String name, int price, int[] rent, Group colorGroup) {
		super(name, price);
		this.rent = rent;
		this.colorGroup = colorGroup;
		
		this.state = State.UNOWNED;
	}
	
	public static Street makeMediterranian() {
		return new Street("Mediterranian Avenue", 60, new int[] {0, 2, 10, 30, 90, 160, 250}, Group.SADDLEBROWN);
	}
	
	public static Street makeBaltic() {
		return new Street("Baltic Avenue", 60, new int[] {0, 4, 20, 60, 180, 320, 450},	Group.SADDLEBROWN);
	}
	
	public String toString() {
		return this.name + " " + this.colorGroup;
	}

	/**
	 * Try to mortgage this property.
	 * @return true if mortgage has been obtained on this street
	 */
	public boolean obtainMortgage() {
		if (this.state == State.OWNED) {
			this.owner.earn(this.getMortgageValue());
			this.state = State.MORTGAGED;
			return true;
		} else {
			return false;
		}
	}
	
	public boolean releaseMortgage() {
		if (this.state == State.MORTGAGED && this.owner.pay(this.getMortgageReleaseAmount())) {
			this.state = State.OWNED;
			return true;
		} else {
			return false;
		}
	}

	private int getMortgageReleaseAmount() {
		int amount = this.getMortgageValue();
		amount += amount / 10;
		return amount;
	}

	/**
	 * Buy a house or a hotel for this street.
	 * Still lacks the check that houses must be build evenly.
	 * @return true if buying the house or hotel was successful.
	 */
	public boolean buyHouse() {
		if (this.state == State.UNOWNED || this.state == State.HOTEL) {
			return false;
		} else if (!this.owner.ownsAllInGroup(this.colorGroup) || this.owner.anyMortgaged(this.colorGroup)) {
			return false;
		} else {
			int i = this.state.ordinal();
			this.state = State.values()[i+1];
			return true;
		}
	}

	@Override
	public boolean inColorGroup(Group colorGroup) {
		return this.getColorGroup() == colorGroup;
	}

	@Override
	public IAction action(Player current, Collection<Player> others, IDice dice) {
		if (this.state == State.UNOWNED) {
			// offer to buy the street
			return new BuyAction("Buy " + this.name + " for $" + this.price , current, this);
		} else {
			// pay rent
			if (current == this.owner) {
				return null;
			} else {
				int amount = this.calculateRent();
				return new PayToAction("Pay $" + amount + " rent for " + this.name + " to " + owner.getName(),
						current, this.owner, amount);
			}
		}
	}

	@Override
	protected boolean isOwned() {
		return !State.UNOWNED.equals(this.state);
	}

	@Override
	protected void setOwnedState() {
		if(this.state == State.UNOWNED) {
			this.state = State.OWNED;	
		}				
	}
	
	/**
	 * Calculate the rent that the owner can charge for this street.
	 * @return the amount of rent.
	 */
	@Override
	public int calculateRent() {
		int amount = rent[this.state.getRentIndex()];
		if (this.state == State.OWNED && owner.ownsAllInGroup(this.colorGroup)) {
			amount *= 2;
		}
		return amount;
	}
}
