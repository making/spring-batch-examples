package com.example.nyusyukkin;

import com.example.batch.file.OutputFileColumn;
import java.util.Date;

/**
 * Parameter class for outputting deposit and withdrawal information to a file.
 */
public class NyusyukkinFileOutput {

	/**
	 * Transaction date.
	 */
	@OutputFileColumn(columnIndex = 0, columnFormat = "yyyyMMdd")
	private Date torihikibi;

	/**
	 * Branch name.
	 */
	@OutputFileColumn(columnIndex = 1)
	private String shitenName;

	/**
	 * Number of deposits.
	 */
	@OutputFileColumn(columnIndex = 2)
	private int nyukinNum;

	/**
	 * Number of withdrawals.
	 */
	@OutputFileColumn(columnIndex = 3)
	private int syukkinNum;

	/**
	 * Total deposits.
	 */
	@OutputFileColumn(columnIndex = 4)
	private int nyukinSum;

	/**
	 * Total withdrawals.
	 */
	@OutputFileColumn(columnIndex = 5)
	private int syukkinSum;

	/**
	 * Gets the transaction date.
	 * @return torihikibi
	 */
	public Date getTorihikibi() {
		return torihikibi;
	}

	/**
	 * Sets the transaction date.
	 * @param torihikibi the transaction date
	 */
	public void setTorihikibi(Date torihikibi) {
		this.torihikibi = torihikibi;
	}

	/**
	 * Gets the branch name.
	 * @return shitenName
	 */
	public String getShitenName() {
		return shitenName;
	}

	/**
	 * Sets the branch name.
	 * @param shitenName the branch name
	 */
	public void setShitenName(String shitenName) {
		this.shitenName = shitenName;
	}

	/**
	 * Gets the number of deposits.
	 * @return nyukinNum
	 */
	public int getNyukinNum() {
		return nyukinNum;
	}

	/**
	 * Sets the number of deposits.
	 * @param nyukinNum the number of deposits
	 */
	public void setNyukinNum(int nyukinNum) {
		this.nyukinNum = nyukinNum;
	}

	/**
	 * Gets the number of withdrawals.
	 * @return syukkinNum
	 */
	public int getSyukkinNum() {
		return syukkinNum;
	}

	/**
	 * Sets the number of withdrawals.
	 * @param syukkinNum the number of withdrawals
	 */
	public void setSyukkinNum(int syukkinNum) {
		this.syukkinNum = syukkinNum;
	}

	/**
	 * Gets the total deposits.
	 * @return nyukinSum
	 */
	public int getNyukinSum() {
		return nyukinSum;
	}

	/**
	 * Sets the total deposits.
	 * @param nyukinSum the total deposits
	 */
	public void setNyukinSum(int nyukinSum) {
		this.nyukinSum = nyukinSum;
	}

	/**
	 * Gets the total withdrawals.
	 * @return syukkinSum
	 */
	public int getSyukkinSum() {
		return syukkinSum;
	}

	/**
	 * Sets the total withdrawals.
	 * @param syukkinSum the total withdrawals
	 */
	public void setSyukkinSum(int syukkinSum) {
		this.syukkinSum = syukkinSum;
	}

	@Override
	public String toString() {
		return "NyusyukkinFileOutput{" + "torihikibi=" + torihikibi + ", shitenName='" + shitenName + '\''
				+ ", nyukinNum=" + nyukinNum + ", syukkinNum=" + syukkinNum + ", nyukinSum=" + nyukinSum
				+ ", syukkinSum=" + syukkinSum + '}';
	}

}
