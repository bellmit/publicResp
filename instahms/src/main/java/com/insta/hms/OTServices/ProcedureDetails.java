/**
 * mithun.saha
 */
package com.insta.hms.OTServices;

/**
 * @author mithun.saha
 *
 */
public class ProcedureDetails {
	private int procedureId;
	private int opDetailsId;
	private String operationPriority;
	private String operationId;
	private String modifier;
	private String priorAuthId;
	private Integer priorAuthModeId;
	private Integer prescribedId;
	public String getPriorAuthId() {
		return priorAuthId;
	}
	public void setPriorAuthId(String priorAuthId) {
		this.priorAuthId = priorAuthId;
	}
	public Integer getPriorAuthModeId() {
		return priorAuthModeId;
	}
	public void setPriorAuthModeId(Integer priorAuthModeId) {
		this.priorAuthModeId = priorAuthModeId;
	}
	public String getModifier() {
		return modifier;
	}
	public void setModifier(String modifier) {
		this.modifier = modifier;
	}
	public int getOpDetailsId() {
		return opDetailsId;
	}
	public void setOpDetailsId(int opDetailsId) {
		this.opDetailsId = opDetailsId;
	}
	public String getOperationId() {
		return operationId;
	}
	public void setOperationId(String operationId) {
		this.operationId = operationId;
	}
	public String getOperationPriority() {
		return operationPriority;
	}
	public void setOperationPriority(String operationPriority) {
		this.operationPriority = operationPriority;
	}
	public int getProcedureId() {
		return procedureId;
	}
	public void setProcedureId(int procedureId) {
		this.procedureId = procedureId;
	}
	public Integer getPrescribedId() {
		return prescribedId;
	}
	public void setPrescribedId(Integer prescribedId) {
		this.prescribedId = prescribedId;
	}
}
