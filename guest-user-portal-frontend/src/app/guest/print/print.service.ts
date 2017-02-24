import { Injectable } from '@angular/core';

@Injectable()
export class PrintService {

  constructor() { }

  printGuest(firstName: string, lastName: string): void {
    var label = dymo.label.framework.openLabelXml(this.labelTemplate);

    let firstname = firstName.replace(/\ /g, '\u00A0');
    label.setObjectText("FORNAVN", firstname);
    let lastname = lastName;
    lastname = lastname.replace(/\ /g, '\u00A0');
    label.setObjectText("ETTERNAVN", lastname);

    var printers = dymo.label.framework.getPrinters();
    if (printers.length == 0) {
      console.log('No printer found? Check link: https://support.microsoft.com/en-us/help/2954953/some-apis-do-not-work-when-they-are-called-in-services-in-windows');
      throw "No DYMO printers are installed. Install DYMO printers.";
    }

    var printerName = "";
    for (var i = 0; i < printers.length; ++i) {
      var printer = printers[i];
      if (printer.printerType == "LabelWriterPrinter") {
        printerName = printer.name;
        break;
      }
    }
    label.print(printerName);
  }

  private labelTemplate: string = `<?xml version="1.0" encoding="utf-8"?>
<DieCutLabel Version="8.0" Units="twips">
	<PaperOrientation>Landscape</PaperOrientation>
	<Id>Address</Id>
	<IsOutlined>false</IsOutlined>
	<PaperName>30252 Address</PaperName>
	<DrawCommands>
		<RoundRectangle X="0" Y="0" Width="1581" Height="5040" Rx="270" Ry="270" />
	</DrawCommands>
	<ObjectInfo>
		<AddressObject>
			<Name>Adresse</Name>
			<ForeColor Alpha="255" Red="0" Green="0" Blue="0" />
			<BackColor Alpha="0" Red="255" Green="255" Blue="255" />
			<LinkedObjectName />
			<Rotation>Rotation0</Rotation>
			<IsMirrored>False</IsMirrored>
			<IsVariable>True</IsVariable>
			<GroupID>-1</GroupID>
			<IsOutlined>False</IsOutlined>
			<HorizontalAlignment>Left</HorizontalAlignment>
			<VerticalAlignment>Middle</VerticalAlignment>
			<TextFitMode>ShrinkToFit</TextFitMode>
			<UseFullFontHeight>True</UseFullFontHeight>
			<Verticalized>False</Verticalized>
			<StyledText>
				<Element>
					<String xml:space="preserve">GJEST</String>
					<Attributes>
						<Font Family="Arial" Size="12" Bold="False" Italic="False" Underline="False" Strikeout="False" />
						<ForeColor Alpha="255" Red="0" Green="0" Blue="0" HueScale="100" />
					</Attributes>
				</Element>
			</StyledText>
			<ShowBarcodeFor9DigitZipOnly>False</ShowBarcodeFor9DigitZipOnly>
			<BarcodePosition>Suppress</BarcodePosition>
			<LineFonts>
				<Font Family="Arial" Size="12" Bold="False" Italic="False" Underline="False" Strikeout="False" />
			</LineFonts>
		</AddressObject>
		<Bounds X="1232.73770491803" Y="363.934426229508" Width="3538.52459016393" Height="360" />
	</ObjectInfo>
	<ObjectInfo>
		<ImageObject>
			<Name>GRAFIKK</Name>
			<ForeColor Alpha="255" Red="0" Green="0" Blue="0" />
			<BackColor Alpha="0" Red="255" Green="255" Blue="255" />
			<LinkedObjectName />
			<Rotation>Rotation0</Rotation>
			<IsMirrored>False</IsMirrored>
			<IsVariable>False</IsVariable>
			<GroupID>-1</GroupID>
			<IsOutlined>False</IsOutlined>
			<Image>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</Image>
			<ScaleMode>Uniform</ScaleMode>
			<BorderWidth>0</BorderWidth>
			<BorderColor Alpha="255" Red="0" Green="0" Blue="0" />
			<HorizontalAlignment>Center</HorizontalAlignment>
			<VerticalAlignment>Center</VerticalAlignment>
		</ImageObject>
		<Bounds X="331" Y="255" Width="840" Height="1238" />
	</ObjectInfo>
	<ObjectInfo>
		<TextObject>
			<Name>FORNAVN</Name>
			<ForeColor Alpha="255" Red="0" Green="0" Blue="0" />
			<BackColor Alpha="0" Red="255" Green="255" Blue="255" />
			<LinkedObjectName />
			<Rotation>Rotation0</Rotation>
			<IsMirrored>False</IsMirrored>
			<IsVariable>False</IsVariable>
			<GroupID>-1</GroupID>
			<IsOutlined>False</IsOutlined>
			<HorizontalAlignment>Left</HorizontalAlignment>
			<VerticalAlignment>Top</VerticalAlignment>
			<TextFitMode>ShrinkToFit</TextFitMode>
			<UseFullFontHeight>True</UseFullFontHeight>
			<Verticalized>False</Verticalized>
			<StyledText>
				<Element>
					<String xml:space="preserve">Fornavng</String>
					<Attributes>
						<Font Family="Arial" Size="12" Bold="False" Italic="False" Underline="False" Strikeout="False" />
						<ForeColor Alpha="255" Red="0" Green="0" Blue="0" HueScale="100" />
					</Attributes>
				</Element>
			</StyledText>
		</TextObject>
		<Bounds X="1223.65573770492" Y="745.327868852458" Width="3686.55737704918" Height="295.819672131148" />
	</ObjectInfo>
	<ObjectInfo>
		<TextObject>
			<Name>ETTERNAVN</Name>
			<ForeColor Alpha="255" Red="0" Green="0" Blue="0" />
			<BackColor Alpha="0" Red="255" Green="255" Blue="255" />
			<LinkedObjectName />
			<Rotation>Rotation0</Rotation>
			<IsMirrored>False</IsMirrored>
			<IsVariable>False</IsVariable>
			<GroupID>-1</GroupID>
			<IsOutlined>False</IsOutlined>
			<HorizontalAlignment>Left</HorizontalAlignment>
			<VerticalAlignment>Top</VerticalAlignment>
			<TextFitMode>ShrinkToFit</TextFitMode>
			<UseFullFontHeight>True</UseFullFontHeight>
			<Verticalized>False</Verticalized>
			<StyledText>
				<Element>
					<String xml:space="preserve">Etternavf</String>
					<Attributes>
						<Font Family="Arial" Size="12" Bold="False" Italic="False" Underline="False" Strikeout="False" />
						<ForeColor Alpha="255" Red="0" Green="0" Blue="0" HueScale="100" />
					</Attributes>
				</Element>
			</StyledText>
		</TextObject>
		<Bounds X="1216.52459016394" Y="1024.31147540984" Width="2701.47540983607" Height="331.475409836066" />
	</ObjectInfo>
</DieCutLabel>`;
}