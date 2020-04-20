package weka.language;

import java.io.Serializable;

public class TripleAttributeVirtualMinMax implements Serializable {
	  
	  private static final long serialVersionUID = -4731451547680000695L;
	  protected int m_attrIndex;
	  protected double m_virtualMin;
	  protected double m_virtualMax;
	  
	  public TripleAttributeVirtualMinMax(int attrIndex, double virtualMin, double virtualMax) {
		  
		  m_attrIndex = attrIndex;
		  m_virtualMin = virtualMin;
		  m_virtualMax = virtualMax;
	  }
	  
	  public int getAttributeIndex() {
		  return m_attrIndex;
	  }
	  
	  public void setAttributeIndex(int attrIndex) {
		  m_attrIndex = attrIndex;
	  }
	  
	  public double getVirtualMinimum() {
		  return m_virtualMin;
	  }
	  
	  public void setMinimum(int virtualMin) {
		  m_virtualMin = virtualMin;
	  }
	  
	  public double getVirtualMaximum() {
		  return m_virtualMax;
	  }
	  
	  public void setMaximum(int virtualMax) {
		  m_virtualMax = virtualMax;
	  }
}
