import {
  Page,
  Text,
  View,
  Document,
  StyleSheet,
  Image,
} from '@react-pdf/renderer';
import { convertStatus } from '../genericComponents/StatusConverter';
import { useTranslation } from 'react-i18next';
import { convertStatusI18n } from '../genericComponents/StatusConverterI18n';

const styles = StyleSheet.create({
  image: {
    width: 236,
    height: 70,
    marginBottom: 10,
  },
  page: {
    padding: 30,
    fontSize: 12,
    fontFamily: 'Helvetica',
    backgroundColor: '#f8f8f8',
  },
  container: {
    backgroundColor: '#ffffff',
    padding: 20,
    border: '1pt solid #e0e0e0',
    borderRadius: 5,
  },
  header: {
    marginBottom: 8,
  },
  title: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 5,
  },
  subTitle: {
    fontSize: 12,
    color: '#777',
  },
  statusSection: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    marginBottom: 20,
  },
  siteSection: {
    flexDirection: 'column',
    marginBottom: 40,
  },
  statusLabel: {
    fontSize: 12,
    color: '#333',
    marginRight: 5,
  },
  statusValue: {
    fontSize: 12,
    fontWeight: 'bold',
    color: 'black',
  },
  section: {
    marginBottom: 20,
  },
  siteInfoRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 8,
  },
  infoRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 8,
  },
  label: {
    width: '40%',
    fontWeight: 'bold',
    color: '#333',
  },
  value: {
    width: '58%',
    backgroundColor: '#f0f0f0',
    padding: 5,
    borderRadius: 4,
    color: '#555',
  },
  opmerkingenBox: {
    backgroundColor: '#f0f0f0',
    padding: 10,
    borderRadius: 4,
    color: '#333',
    minHeight: 60,
  },
  button: {
    padding: 12,
    backgroundColor: '#e53935',
    color: '#ffffff',
    textAlign: 'center',
    borderRadius: 4,
    fontWeight: 'bold',
    fontSize: 14,
    marginTop: 20,
  },
  footerNote: {
    marginTop: 10,
    fontSize: 10,
    color: '#999',
    textAlign: 'center',
  },
});

// Format the datetime for the start and end times
const formatDateTime = (datetime) => {
  return `${new Date(datetime).toLocaleDateString()} ${new Date(datetime).toLocaleTimeString()}`;
};

const PdfDocument = ({ data, base64Logo }) => {

  const convertedStatus = convertStatus(data.rawStatus);

  const {t} = useTranslation();

  return (
    <Document>
      <Page size="A4" style={styles.page}>
        <View style={styles.container}>
          <View style={styles.header}>
            <Text style={styles.title}>{t('maintenance.report')}</Text>
            <Text style={styles.subTitle}>{t('machine')}: {data.machine.code}</Text>
          </View>

          <View style={styles.statusSection}>
            <Text style={styles.statusLabel}>{t('maintenance.status')}:</Text>
            <Text
              style={[
                styles.statusValue,
                { color: convertedStatus.color },
              ]}
            >
              {String(convertStatusI18n(t, data.rawStatus))}
            </Text>
          </View>

          {/* Site Section */}
          <View style={styles.siteSection}>
            <Text style={styles.title}>{t('site')}</Text>
            
            {/* Site Name */}
            <View style={styles.siteInfoRow}>
              <Text style={styles.label}>{t('site.name')}</Text>
              <Text style={styles.value}>{data.machine.site.sitename}</Text>
            </View>

            {/* Responsible Person */}
            <View style={styles.siteInfoRow}>
              <Text style={styles.label}>{t('site.responsible')}</Text>
              <Text style={styles.value}>
                {`${data.machine.site.verantwoordelijke.firstname} ${data.machine.site.verantwoordelijke.lastname}`}
              </Text>
            </View>

            {/* Site Address (if available) */}
            {data.machine.site.adres && (
              <View style={styles.siteInfoRow}>
                <Text style={styles.label}>{t('site.address')}</Text>
                <Text style={styles.value}>{data.machine.site.adres}</Text>
              </View>
            )}
          </View>

          {/* Maintenance Details Section */}
          <View style={styles.section}>
            <Text style={styles.title}>{t('maintenance-details')}</Text>

            <View style={styles.infoRow}>
              <Text style={styles.label}>{t('number')}</Text>
              <Text style={styles.value}>{data.id}</Text>
            </View>

            <View style={styles.infoRow}>
              <Text style={styles.label}>{t('maintenance.starttime')}</Text>
              <Text style={styles.value}>{formatDateTime(data.startdate)}</Text>
            </View>

            <View style={styles.infoRow}>
              <Text style={styles.label}>{t('maintenance.endttime')}</Text>
              <Text style={styles.value}>{formatDateTime(data.enddate)}</Text>
            </View>

            <View style={styles.infoRow}>
              <Text style={styles.label}>{t('maintenance.technician')}</Text>
              <Text style={styles.value}>{data.technieker}</Text>
            </View>

            <View style={styles.infoRow}>
              <Text style={styles.label}>{t('maintenance.reason')}</Text>
              <Text style={styles.value}>{data.reason}</Text>
            </View>

            <Text style={{ fontWeight: 'bold', marginBottom: 5 }}>{t('maintenance.comments')}</Text>
            <Text style={styles.opmerkingenBox}>{data.comments}</Text>
          </View>

          <Image src={base64Logo} style={styles.image} />
        </View>
      </Page>
    </Document>
  );
};

export default PdfDocument;
