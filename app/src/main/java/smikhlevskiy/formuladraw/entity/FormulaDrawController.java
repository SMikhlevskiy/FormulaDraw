package smikhlevskiy.formuladraw.entity;

/*
 * Controller of FormulaDraw
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import smikhlevskiy.formuladraw.R;
import smikhlevskiy.formuladraw.model.FormulaDrawBaseData;
import smikhlevskiy.formuladraw.model.ReversePolishNotation;
import smikhlevskiy.formuladraw.ui.GraphicView;
import smikhlevskiy.formuladraw.util.FDConstants;

public class FormulaDrawController {

	private String[] formulas = new String[FDConstants.colorSpinnerLines.length];
	private boolean[] isdYdT = new boolean[FDConstants.colorSpinnerLines.length];

	private Context context;
	private Handler outHandler;
	private SharedPreferences formulaDrawPreferences;
	private int currentLine = 0;

	private FormulaDrawBaseData formulaDrawBaseData;// To-DO

	/*
	 * ---------------------
	 */

	public FormulaDrawController(Context context, Handler outHandler) {
		super();
		this.context = context;
		this.outHandler = outHandler;
		FormulaDrawBaseData formulaDrawBaseData = new FormulaDrawBaseData(context);// To-DO
		for (int i=0;i<FDConstants.colorSpinnerLines.length;i++)
			formulas[i]="";

		// TODO Auto-generated constructor stub
	}

	/*
	 * Draw Graphic on GraphicView
	 */
	public void drawGraphic(GraphicView graphicView, String textFormula, double xStart, double xEnd) {
		Log.i("Main activity", "Start Draw Graphic");
		
		if (xStart >= xEnd) {
			Message message = outHandler.obtainMessage();
			message.what = FDConstants.OUT_TEXT_ERROR_MESSAGE;
			message.obj = context.getString(R.string.errorDiapazon);
			outHandler.sendMessage(message);
			return;
		}
		Log.i("Main activity", "Compile formulas");
		ReversePolishNotation reversePolishNotation[] = new ReversePolishNotation[FDConstants.colorSpinnerLines.length];
		for (int i = 0; i < FDConstants.colorSpinnerLines.length; i++) 
		{
			Log.i("Main activity", i+"");
			Log.i("Main activity", getFormulas(i).length()+"");
			if (getFormulas(i).length()==0){ 
				reversePolishNotation[i]=null;
				continue; 
				}  
			
			reversePolishNotation[i] = new ReversePolishNotation();
			reversePolishNotation[i].setFormula(getFormulas(i));
			reversePolishNotation[i].compile();
		}
		Log.i("Main activity", "End compile formulas");
		graphicView.setReversePolishNotation(reversePolishNotation);
		graphicView.setIsdYdT(isdYdT);
		graphicView.setMinMax(xStart, xEnd);
		graphicView.setDrawCustomCanvas(true);
		Log.i("Main activity", "Draw formulas");
		graphicView.invalidate();
	}

	/*
	 * Cack Value f(x) and send result to MainActivity by Handler
	 */
	public void cakulator(String textFormula, double x) {
		ReversePolishNotation reversePolishNotation = new ReversePolishNotation();
		reversePolishNotation.setFormula(textFormula);
		reversePolishNotation.compile();

		Message message = outHandler.obtainMessage();

		try {
			double val = reversePolishNotation.calculation(x);

			message.obj = new String("f(" + x + ")=" + val);
			message.what = FDConstants.OUT_TEXT_INFO_MESSAGE;
			outHandler.sendMessage(message);

		} catch (ArithmeticException e) {
			message.obj = new String(context.getString(R.string.mathError));
			message.what = FDConstants.OUT_TEXT_ERROR_MESSAGE;
			outHandler.sendMessage(message);

		}

	}

	/**
	 * Save state Workspace in Preferces: formulas and min/max
	 */
	public void saveWorkspace(String xMin, String xMax) {
		SharedPreferences.Editor editor = formulaDrawPreferences.edit();
		for (int i = 0; i < FDConstants.colorSpinnerLines.length; i++) {
			editor.putString(FDConstants.APP_PREFERENCES_TEXTFormula + i, getFormulas(i));
			editor.putBoolean(FDConstants.APP_PREFERENCES_dYdT + i, getdYdT(i));
		}

		editor.putString("xMin", xMin);
		editor.putString("xMax", xMax);

		editor.commit();
	}

	/**
	 * Save state Workspace in Preferces: formulas and min/max
	 */
	public void saveNullWorkspace() {
		SharedPreferences.Editor editor = formulaDrawPreferences.edit();
		for (int i = 0; i < FDConstants.colorSpinnerLines.length; i++) {
			if (i == 0)
				editor.putString(FDConstants.APP_PREFERENCES_TEXTFormula + i, "x*x+x");
			else
				editor.putString(FDConstants.APP_PREFERENCES_TEXTFormula + i, "");
			editor.putBoolean(FDConstants.APP_PREFERENCES_dYdT + i, false);
		}

		editor.putString("xMin", "-10");
		editor.putString("xMax", "10");

		editor.commit();
	}

	/**
	 * --------Read Prefereces-------------- Text formulas
	 */
	public void readPreferences() {
		String xMinText="0";

		
		formulaDrawPreferences = context.getSharedPreferences(FDConstants.APP_PREFERENCES, context.MODE_PRIVATE);
		
	
			
		

		xMinText = formulaDrawPreferences.getString("xMin", "-10");
		
		
		
		String xMaxText = formulaDrawPreferences.getString("xMax", "10");
		double xMin = 0.0;
		double xMax = 10.0;
		
		Log.i("Main activity", "Read xMin,xMax");
		
		try {
			
		if (xMinText.length() > 0)
			xMin = new Double(xMinText);
		if (xMaxText.length() > 0)
			xMax = new Double(xMaxText);
		} catch (Exception e) {
			xMin = new Double(-11.0);
			xMax= new Double(10.0);
		}

		Log.i("Main activity", "xMin=" + xMin);
		Log.i("Main activity", "xMax=" + xMax);

		Message message = outHandler.obtainMessage();
		message.what = FDConstants.CHANGE_XLIMITS;
		double xMinMaxa[] = new double[2];
		xMinMaxa[0] = xMin;
		xMinMaxa[1] = xMax;
		message.obj = xMinMaxa;
		outHandler.sendMessage(message);

		for (int i = 0; i < FDConstants.colorSpinnerLines.length; i++) {
			String s = "";
			if (i == 0)
				s = "x*x+x";

			setFormulas(formulaDrawPreferences.getString(FDConstants.APP_PREFERENCES_TEXTFormula + i, s), i);
			Log.i("Main activity", "formula[" + i + "]=" + getFormulas(i));

			setdYdT(formulaDrawPreferences.getBoolean(FDConstants.APP_PREFERENCES_dYdT + i, false), i);
			Log.i("Main activity", "dY/dT[" + i + "]=" + getdYdT(i));

			// formulaDrawController.setFormulas("",i);
		}
	}

	/**
	 * 
	 * ------Save Preferences Text formulas
	 */
	public void saveUserFunction(String name, String textFunction) {
//		ParseUser user = ParseUser.getCurrentUser();
//		ParseObject post = new ParseObject("UserDatas");
//		post.put("formula", textFunction);
//		post.put("name", name);
//		post.put("user", user);
//		post.saveInBackground();
	}

	/**
	 * Form function insert
	 * 
	 */
	public String formFunctionInsert(String textFormula) {

		ReversePolishNotation rpn = new ReversePolishNotation();
		rpn.setFormula(textFormula);
		if ((rpn.textToFunction(textFormula) == FDConstants.TypeFunction.XVALUE)
				|| (rpn.textToFunction(textFormula) == FDConstants.TypeFunction.E)
				|| (rpn.textToFunction(textFormula) == FDConstants.TypeFunction.Pi))
			return textFormula;
		else
			return textFormula + "(x)";

	}

	public int getCurrentLine() {
		return currentLine;
	}

	public void setCurrentLine(int currentLine) {
		this.currentLine = currentLine;
	}

	public String getFormulas(int i) {
		return formulas[i];
	}

	public void setFormulas(String formula, int i) {
		this.formulas[i] = formula;
	}

	public void setdYdT(boolean isdYdT, int i) {
		this.isdYdT[i] = isdYdT;
	}

	public boolean getdYdT(int i) {
		return this.isdYdT[i];
	}

}
