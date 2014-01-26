package ch.epfl.cmiapp.core;

import java.util.LinkedList;
import java.util.Queue;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebLoadedConfiguration extends Configuration
{
	Builder builder;

	protected WebLoadedConfiguration(Document document, Equipment equipment)
	{
		super(equipment);
		htmlExtract(document);
	}
	
	// Expected document is the cmi main reservation page with a (configurable) tool selected
	private void htmlExtract(Document document)
	{
		Element divElement = document.select("div[style=margin:3;background-color:#FFFFCC]").first();
		Elements tdElements = divElement.select("td[onmouseover]");
		
		Queue<String> titles = new LinkedList<String>();
		for (Element td : tdElements) titles.add(td.ownText());
		
		Elements selectElements = divElement.select("select[name]");
		
		for (Element selectElement : selectElements) 
		{
			Setting setting = builder.createSetting();
			
			setting.display = 1;
			setting.id = selectElement.attr("name");
			setting.title = titles.poll();
			setting.name = setting.title;
			
			Elements optionElements = selectElement.getElementsByTag("option");
			for (Element optionElement : optionElements)
			{
				Option option = new Option();
				option.value = optionElement.attr("value");
				option.title = optionElement.ownText();
				option.name = option.title;
				
				setting.options.add(option);
			}	
		}
	}
	
}


/*


<div style="margin:3;background-color:#FFFFCC">
	<table border="0" cellspacing="0" cellpadding="3" style="font-size:8pt">
		<tr style="font-weight:bold">
			<td width="90">&nbsp;</td>
			<td width="80">&nbsp;</td>
			<td width="80" onmouseover="this.style.cursor='pointer';return escape('<b>Substrate size : Standard = 100mm</b><br><b>100mm</b> : standard<br><b>150mm Si</b> : Attention No Spray Coating !<br>');">Substrate size</td>
			<td width="80" onmouseover="this.style.cursor='pointer';return escape('<b>Syringe & Spray : Default in syringe is LOR (to be used with AZ1512)</b><br><b>nLoF 1 (400-800)</b> : Image reversal 400 to 800nm<br><b>nLoF 2 (1.3-3.5)</b> : Image reversal 1.3 to 3.5mu<br><b>TI35ES</b> : TI: Fridge stored<br><b>Spray 4:8:90</b> : Use Spray Coating Arm. Std<br><b>Spray 4:11:90</b> : Use Spray Coating Arm. Planar<br>');">Syringe & Spray</td>
			<td width="80" onmouseover="this.style.cursor='pointer';return escape('<b>Developer : Developer lines</b><br><b>Developer</b> : <br>');">Developer</td>
			<td width="80" onmouseover="this.style.cursor='pointer';return escape('<b>Resist 1 in line : Automatic line 1</b><br><b>AZ1512</b> : <br>');">Resist 1 in line</td>
			<td width="80" onmouseover="this.style.cursor='pointer';return escape('<b>Resist 2 in line : Automatic line 2</b><br><b>AZ9260</b> : <br>');">Resist 2 in line</td>
		</tr>
		<tr>
			<td colspan="2" style="font-size:11pt;color:red">Set up your configuration</td>
			<td>
				<select name="list0" id="list0"  style="font-size:9px;width:72px">
					<option value="0">N/A
					<option  style="color:#000000;background:#CCFFFF" value="630" >100mm
					<option  style="color:#000000;background:#33CCCC" value="629" >150mm Si
				</select>
			</td>
			<td>
				<select name="list1" id="list1"  style="font-size:9px;width:124px">
					<option value="0">N/A
					<option  style="color:#000000;background:#FFFF99" value="640" >nLoF 1 (400-800)
					<option  style="color:#000000;background:#FFCC99" value="641" >nLoF 2 (1.3-3.5)
					<option  style="color:#000000;background:#FF99CC" value="642" >TI35ES
					<option  style="color:#000000;background:#CCFFCC" value="643" >Spray 4:8:90
					<option  style="color:#000000;background:#99CC00" value="644" >Spray 4:11:90
				</select>
			</td>
			<td>
				<select name="list2" id="list2"  style="font-size:9px;width:78.5px">
					<option value="0">N/A
					<option  style="color:#000000;background:#FFFF00" value="645" >Developer
				</select>
			</td>
			<td>
				<select name="list3" id="list3"  style="font-size:9px;width:60px">
					<option value="0">N/A
					<option  style="color:#000000;background:#FF00FF" value="646" >AZ1512
				</select>
			</td>
			<td>
				<select name="list4" id="list4"  style="font-size:9px;width:60px">
					<option value="0">N/A
					<option  style="color:#000000;background:#FF6600" value="647" >AZ9260
				</select>
			</td>
		</tr>		
	</table>
</div>

*/