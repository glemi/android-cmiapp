package ch.epfl.cmiapp.adapters;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.cmiapp.R;
import ch.epfl.cmiapp.R.color;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public abstract class SectionedListAdapter extends BaseAdapter 
{
	public enum RowItemType 
	{
		LIST_ITEM, SECTION_HEADER
	}
	
	private /*static*/ class RowItem
		implements java.lang.Comparable<RowItem>
	{
		// index is the inner position of the item in the derived adapter
		public RowItemType rowItemType;
		public Integer index; 
		public String section;
		
		public RowItem(int index, String section, RowItemType rowItemType)
		{
			// in case of SECTION_HEADER the index is the inner position of 
			// the first item under the added header. 
			this.rowItemType = rowItemType;
			this.index = index;
			this.section = section;
		}

		public int compareTo(RowItem other) 
		{
			int rel = SectionedListAdapter.this.compareSectionsAt(this.index, other.index);
			
			if (rel == 0)
				rel = this.index.compareTo(other.index);
			
			if (rel == 0)
				rel = this.rowItemType == RowItemType.SECTION_HEADER ? -1 : +1;
			
			return rel;
		}
	}
	
	boolean sectionsEnabled = true;
	List<String>  sections = new ArrayList<String>();
	List<RowItem> rowItems = new ArrayList<RowItem>();
	
	public abstract View getItemView(int position, View convertView, ViewGroup parent); 
	public abstract int getItemCount();	
	public abstract String getSection(int position);
	
	public SectionedListAdapter() { }
	
	public final int getCount() 
	{
		return getSectionCount() + getItemCount();
	}
	
	public int getSectionCount()
	{
		return sections.size();
	}
	
	public void notifyDataSetChanged()
	{
		if (sectionsEnabled) 
			refresh();
		super.notifyDataSetChanged();
	}
	
	public void setSectionsEnabled(boolean value)
	{
		sectionsEnabled = value;
		if (sectionsEnabled) refresh();
	}
	
	public boolean isHeaderPosition(int position)
	{
		if (!sectionsEnabled) return false;
		if (sections.isEmpty() && getItemCount() > 0 || rowItems.size() <= position) refresh();
		RowItem rowItem = rowItems.get(position);
		return rowItem.rowItemType == RowItemType.SECTION_HEADER;
	}
	
	private void refresh()
	{
		
		int itemCount = getItemCount();
		
		sections.clear();
		rowItems.clear();
		
		for (int index = 0; index < itemCount; index++)
		{
			String section = getSection(index);
			
			if (!sections.contains(section) && sectionsEnabled)
			{
				sections.add(section);
				rowItems.add(new RowItem(index, section, RowItemType.SECTION_HEADER)); 
			}
			rowItems.add(new RowItem(index, section,  RowItemType.LIST_ITEM));
		}
		java.util.Collections.sort(rowItems);
	}
	
	protected int translatePosition(int outerPosition)
	{
		if (!sectionsEnabled) return outerPosition;
		if (rowItems.size() <= outerPosition) refresh();
		return rowItems.get(outerPosition).index;
	}

	public Object getItem(int position) 
	{
		// this method will never get called.
		if (sections.isEmpty() && getItemCount() > 0)
			refresh();
		
		RowItem rowItem = rowItems.get(position);
		switch (rowItem.rowItemType)
		{
		case LIST_ITEM: 		return getItem(rowItem.index);
		case SECTION_HEADER: 	return rowItem.section; 
		default:				return null;
		}
	}

	public long getItemId(int position) 
	{
		if (this.isHeaderPosition(position))
		{
			RowItem rowItem = rowItems.get(position);
			long hash = rowItem.section.hashCode();
			// use the section hash code and add 
			// a flag at bit#56 
			return hash | 1l << 56; 
		}
		else return 0;
	}
	
	@Override
	public boolean hasStableIds()
	{
		// this you want to change this then override! 
		return false; 
	}
	
	public final boolean getEnabled(int position)
	{
		if (!sectionsEnabled) return true;
		if (this.isHeaderPosition(position))
			return false;
		else
			return getItemEnabled(position);
	}
	
	public boolean getItemEnabled(int position)
	{
		return true;
	}
	
	// default implementation; overriding is recommended
	public int compareSectionsAt(int position1, int position2)
	{
		String section1 = getSection(position1);
		String section2 = getSection(position2);
		
		// section titles lexical ordering
		return section1.compareTo(section2);
	}

	public final View getView(int position, View convertView, ViewGroup parent) 
	{	
		if (this.isHeaderPosition(position))
			return getHeaderView(position, convertView, parent);
		else  
			return getItemView(translatePosition(position), convertView, parent);
	}
	
	public View getHeaderView(int position, View convertView, ViewGroup parent) 
	{
		TextView textView;
		Context context = parent.getContext();
		
		
		// it's ok, if getItemViewType works correctly then convertView will always be 
		// the right view, i.e. a textView, no need to check using instanceof
		if (convertView instanceof TextView) 
		{
			textView = (TextView) convertView;
		}
		else
		{
			context = parent.getContext();
			textView = new TextView(context);
		}
		
		RowItem rowItem = rowItems.get(position);
		
		textView.setText(rowItem.section);
		textView.setTextSize(16);
		textView.setPadding(18, 10, 16, 10);
		textView.setBackgroundColor(context.getResources().getColor(R.color.listHeaderBackground));
		textView.setTextColor(context.getResources().getColor(R.color.listHeaderForeground));
		textView.setEnabled(false);
		
		return textView;
	}
	
	public final int getViewTypeCount()
	{ 
		// SectionedListAdapter currently only works with lists
		// that have only one view type.
		return 2;
	}
	
	public int getItemViewType(int position) 
	{
		return this.isHeaderPosition(position) ? 1 : 0;
	}
	
}
